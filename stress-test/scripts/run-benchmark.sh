#!/bin/bash
# Run stress-test scenarios with preflight checks and auto batch resolution.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STRESS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
BASE_URL="${BASE_URL:-http://localhost:8080/api}"
JMETER_BIN="${JMETER_BIN:-jmeter}"
# JMeter JVM heap: prevent segfault under high concurrency (Phase 6/7 mixed workload)
export JVM_ARGS="${JVM_ARGS:--Xms512m -Xmx2g}"
ADMIN_TOKEN_CSV="${ADMIN_TOKEN_CSV:-${STRESS_ROOT}/data/tokens-admin.csv}"
STUDENT_TOKEN_CSV="${STUDENT_TOKEN_CSV:-${STRESS_ROOT}/data/tokens-student.csv}"
JMETER_DIR="${STRESS_ROOT}/jmeter"
RESULT_ROOT="${STRESS_ROOT}/results"
RESULT_DIR="${RESULT_ROOT}/$(date +%Y%m%d_%H%M%S)"
LOG_DIR="${RESULT_DIR}/logs"
MANIFEST_FILE="${RESULT_DIR}/manifest.csv"
ONLY_PHASES="${ONLY_PHASES:-}"
SKIP_PHASES="${SKIP_PHASES:-}"

mkdir -p "$RESULT_DIR" "$LOG_DIR"
echo "phase,start_time,end_time,status,exit_code,jmx,jtl,monitor_csv" > "$MANIFEST_FILE"

source <(bash "${SCRIPT_DIR}/resolve-batch-ids.sh" --format=env)
bash "${SCRIPT_DIR}/preflight-check.sh"

ADMIN_TOKEN=$(tail -n +2 "$ADMIN_TOKEN_CSV" | head -n 1 | cut -d',' -f1)
if [ -z "$ADMIN_TOKEN" ]; then
    echo "Admin token csv is empty: $ADMIN_TOKEN_CSV" >&2
    exit 1
fi

CONFLICT_TOKEN_CSV="${RESULT_DIR}/tokens-student-conflict.csv"
{
    head -n 1 "$STUDENT_TOKEN_CSV"
    tail -n +2 "$STUDENT_TOKEN_CSV" | head -n 1
} > "$CONFLICT_TOKEN_CSV"

MONITOR_PID=""
CURRENT_PHASE=""
CURRENT_MONITOR_CSV=""

cleanup() {
    stop_monitor
}

trap cleanup EXIT
trap 'echo "Interrupted."; exit 130' INT TERM

start_monitor() {
    local phase_name="$1"
    local monitor_csv="${RESULT_DIR}/monitor_${phase_name}.csv"
    echo ">>> start monitor: $phase_name"
    PHASE_NAME="$phase_name" bash "${SCRIPT_DIR}/monitor.sh" "$ADMIN_TOKEN" "$monitor_csv" 1 \
        > "${LOG_DIR}/monitor_${phase_name}.log" 2>&1 &
    MONITOR_PID=$!
    sleep 1
}

stop_monitor() {
    if [ -n "$MONITOR_PID" ] && kill -0 "$MONITOR_PID" 2>/dev/null; then
        kill "$MONITOR_PID" 2>/dev/null || true
        wait "$MONITOR_PID" 2>/dev/null || true
    fi
    MONITOR_PID=""
}

should_run_phase() {
    local phase="$1"

    if [ -n "$ONLY_PHASES" ]; then
        case ",${ONLY_PHASES}," in
            *,"${phase}",*) ;;
            *)
                return 1
                ;;
        esac
    fi

    if [ -n "$SKIP_PHASES" ]; then
        case ",${SKIP_PHASES}," in
            *,"${phase}",*)
                return 1
                ;;
        esac
    fi

    return 0
}

append_manifest() {
    local phase="$1"
    local start_time="$2"
    local end_time="$3"
    local status="$4"
    local exit_code="$5"
    local jmx_file="$6"
    local jtl_file="$7"
    local monitor_csv="$8"
    printf '%s,%s,%s,%s,%s,%s,%s,%s\n' \
        "$phase" "$start_time" "$end_time" "$status" "$exit_code" "$jmx_file" "$jtl_file" "$monitor_csv" >> "$MANIFEST_FILE"
}

run_jmeter() {
    local jmx_file="$1"
    local token_csv="$2"
    local jtl_file="$3"
    local log_file="$4"
    shift 4

    echo ">>> run ${jmx_file} -> ${jtl_file}"
    "$JMETER_BIN" -n \
        -t "${JMETER_DIR}/${jmx_file}" \
        -l "$jtl_file" \
        -Jbase_url="$BASE_URL" \
        -Jtoken_csv="$token_csv" \
        -Jbatch_submit="$BATCH_LARGE" \
        -Jbatch_query="$BATCH_QUERY" \
        -Jbatch_small="$BATCH_SMALL" \
        -Jbatch_medium="$BATCH_MEDIUM" \
        -Jbatch_large="$BATCH_LARGE" \
        "$@" \
        -j "$log_file" \
        > /dev/null 2>&1
}

run_phase() {
    local phase="$1"
    local jmx_file="$2"
    local token_csv="$3"
    shift 3

    local start_time end_time exit_code
    local jtl_file="${RESULT_DIR}/${phase}_$(basename "$jmx_file" .jmx).jtl"
    local log_file="${LOG_DIR}/${phase}_$(basename "$jmx_file" .jmx).log"
    local monitor_csv="${RESULT_DIR}/monitor_${phase}.csv"

    if ! should_run_phase "$phase"; then
        echo ">>> skip ${phase}"
        append_manifest "$phase" "$(date +%Y-%m-%dT%H:%M:%S)" "$(date +%Y-%m-%dT%H:%M:%S)" "SKIPPED" "0" "$jmx_file" "$jtl_file" "$monitor_csv"
        return 0
    fi

    CURRENT_PHASE="$phase"
    CURRENT_MONITOR_CSV="$monitor_csv"
    start_time="$(date +%Y-%m-%dT%H:%M:%S)"
    start_monitor "$phase"

    if run_jmeter "$jmx_file" "$token_csv" "$jtl_file" "$log_file" "$@"; then
        exit_code=0
        stop_monitor
        end_time="$(date +%Y-%m-%dT%H:%M:%S)"
        append_manifest "$phase" "$start_time" "$end_time" "SUCCESS" "$exit_code" "$jmx_file" "$jtl_file" "$monitor_csv"
        bash "${SCRIPT_DIR}/slow-sql-monitor.sh" "${RESULT_DIR}/slow-sql_${phase}.json" > "${LOG_DIR}/slow-sql_${phase}.log" 2>&1 || true
        CURRENT_PHASE=""
        CURRENT_MONITOR_CSV=""
        return 0
    else
        exit_code=$?
        stop_monitor
        end_time="$(date +%Y-%m-%dT%H:%M:%S)"
        append_manifest "$phase" "$start_time" "$end_time" "FAILED" "$exit_code" "$jmx_file" "$jtl_file" "$monitor_csv"
        bash "${SCRIPT_DIR}/slow-sql-monitor.sh" "${RESULT_DIR}/slow-sql_${phase}.json" > "${LOG_DIR}/slow-sql_${phase}.log" 2>&1 || true
        CURRENT_PHASE=""
        CURRENT_MONITOR_CSV=""
        return "$exit_code"
    fi
}

echo "======================================"
echo "Stress benchmark"
echo "======================================"
echo "base url:     $BASE_URL"
echo "results dir:  $RESULT_DIR"
echo "PT-SMALL:     $BATCH_SMALL"
echo "PT-MEDIUM:    $BATCH_MEDIUM"
echo "PT-LARGE:     $BATCH_LARGE"
echo "PT-QUERY:     $BATCH_QUERY"
echo "======================================"

run_phase "phase1_submit_normal" \
    "application-submit-normal.jmx" "$STUDENT_TOKEN_CSV"

run_phase "phase2_submit_conflict" \
    "application-submit-conflict.jmx" "$CONFLICT_TOKEN_CSV"

run_phase "phase3_evaluate" \
    "evaluation-execute.jmx" "$ADMIN_TOKEN_CSV"

run_phase "phase4_result_page" \
    "result-page.jmx" "$ADMIN_TOKEN_CSV"

run_phase "phase5_result_export" \
    "result-export.jmx" "$ADMIN_TOKEN_CSV"

run_phase "phase6_mixed_workload" \
    "mixed-workload.jmx" "$ADMIN_TOKEN_CSV" -Jstudent_token_csv="$STUDENT_TOKEN_CSV" || true

run_phase "phase7_stability_mixed" \
    "mixed-workload.jmx" "$ADMIN_TOKEN_CSV" \
        -Jstudent_token_csv="$STUDENT_TOKEN_CSV" -Jrun_seconds=1800 -Jpage_threads=30 -Jsubmit_threads=8 -Jevaluate_threads=2 -Jexport_threads=2

# Merge per-phase slow SQL files into a unified report
echo ">>> merging per-phase slow SQL reports..."
bash "${SCRIPT_DIR}/slow-sql-monitor.sh" "${RESULT_DIR}/slow-sql.json" > "${LOG_DIR}/slow-sql.log" 2>&1 || true

bash "${SCRIPT_DIR}/analyze-results.sh" "${RESULT_DIR}" > "${LOG_DIR}/analyze-results.log" 2>&1 || true

echo "Completed. Results: $RESULT_DIR"
echo "Summary report: ${RESULT_DIR}/summary-report.md"
echo "Manifest: ${MANIFEST_FILE}"
echo ""
echo "Suggested cleanup steps:"
echo "  mysql -u root -p < stress-test/scripts/cleanup-data.sql"
echo "  bash stress-test/scripts/cleanup-redis.sh"
