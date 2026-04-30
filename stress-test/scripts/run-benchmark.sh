#!/bin/bash
# Run stress-test scenarios with preflight checks and auto batch resolution.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_URL="${BASE_URL:-http://localhost:8080/api}"
JMETER_BIN="${JMETER_BIN:-jmeter}"
ADMIN_TOKEN_CSV="${ADMIN_TOKEN_CSV:-${SCRIPT_DIR}/../data/tokens-admin.csv}"
STUDENT_TOKEN_CSV="${STUDENT_TOKEN_CSV:-${SCRIPT_DIR}/../data/tokens-student.csv}"
JMETER_DIR="${SCRIPT_DIR}/../jmeter"
RESULT_DIR="${SCRIPT_DIR}/../results/$(date +%Y%m%d_%H%M%S)"
LOG_DIR="${RESULT_DIR}/logs"

mkdir -p "$RESULT_DIR" "$LOG_DIR"

source <(bash "${SCRIPT_DIR}/resolve-batch-ids.sh")
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

run_jmeter() {
    local phase="$1"
    local jmx_file="$2"
    local token_csv="$3"
    shift 3

    local jtl_file="${RESULT_DIR}/${phase}_$(basename "$jmx_file" .jmx).jtl"
    local log_file="${LOG_DIR}/${phase}_$(basename "$jmx_file" .jmx).log"

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
    shift
    start_monitor "$phase"
    "$@"
    stop_monitor
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
    run_jmeter "phase1_submit_normal" "application-submit-normal.jmx" "$STUDENT_TOKEN_CSV"

run_phase "phase2_submit_conflict" \
    run_jmeter "phase2_submit_conflict" "application-submit-conflict.jmx" "$CONFLICT_TOKEN_CSV"

run_phase "phase3_evaluate" \
    run_jmeter "phase3_evaluate" "evaluation-execute.jmx" "$ADMIN_TOKEN_CSV"

run_phase "phase4_result_page" \
    run_jmeter "phase4_result_page" "result-page.jmx" "$ADMIN_TOKEN_CSV"

run_phase "phase5_result_export" \
    run_jmeter "phase5_result_export" "result-export.jmx" "$ADMIN_TOKEN_CSV"

run_phase "phase6_mixed_workload" \
    run_jmeter "phase6_mixed_workload" "mixed-workload.jmx" "$ADMIN_TOKEN_CSV" -Jstudent_token_csv="$STUDENT_TOKEN_CSV"

run_phase "phase7_stability_mixed" \
    run_jmeter "phase7_stability_mixed" "mixed-workload.jmx" "$ADMIN_TOKEN_CSV" \
        -Jstudent_token_csv="$STUDENT_TOKEN_CSV" -Jrun_seconds=1800 -Jpage_threads=30 -Jsubmit_threads=8 -Jevaluate_threads=2 -Jexport_threads=2

bash "${SCRIPT_DIR}/slow-sql-monitor.sh" "${RESULT_DIR}/slow-sql.json" > "${LOG_DIR}/slow-sql.log" 2>&1 || true
bash "${SCRIPT_DIR}/analyze-results.sh" "${RESULT_DIR}" > "${LOG_DIR}/analyze-results.log" 2>&1 || true

echo "Completed. Results: $RESULT_DIR"
echo "Summary report: ${RESULT_DIR}/summary-report.md"
echo ""
echo "Suggested cleanup steps:"
echo "  mysql -u root -p < stress-test/scripts/cleanup-data.sql"
echo "  bash stress-test/scripts/cleanup-redis.sh"
