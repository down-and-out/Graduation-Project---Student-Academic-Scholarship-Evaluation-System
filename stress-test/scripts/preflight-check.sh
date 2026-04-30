#!/bin/bash
# Validate local stress-test prerequisites.
# Severity levels:
#   CRITICAL — blocks execution (JMeter missing, empty token CSV)
#   WARN     — reports but does not block (health check, single metric)
#   PASS     — all good

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STRESS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
BASE_URL="${BASE_URL:-http://localhost:8080/api}"
ADMIN_TOKEN_CSV="${ADMIN_TOKEN_CSV:-${STRESS_ROOT}/data/tokens-admin.csv}"
STUDENT_TOKEN_CSV="${STUDENT_TOKEN_CSV:-${STRESS_ROOT}/data/tokens-student.csv}"
JMETER_BIN="${JMETER_BIN:-jmeter}"

PASS_COUNT=0
WARN_COUNT=0
FAIL_COUNT=0
CRITICAL_COUNT=0

pass() {
    PASS_COUNT=$((PASS_COUNT + 1))
    printf '[PASS] %s\n' "$1"
}

warn() {
    WARN_COUNT=$((WARN_COUNT + 1))
    printf '[WARN] %s\n' "$1"
}

fail() {
    FAIL_COUNT=$((FAIL_COUNT + 1))
    printf '[FAIL] %s\n' "$1"
}

critical() {
    CRITICAL_COUNT=$((CRITICAL_COUNT + 1))
    FAIL_COUNT=$((FAIL_COUNT + 1))
    printf '[CRITICAL] %s\n' "$1"
}

check_file() {
    local path="$1"
    local label="$2"
    if [ ! -f "$path" ]; then
        critical "${label}: missing file ${path}"
        return 1
    fi
    pass "${label}: ${path}"
    return 0
}

check_command() {
    local command_name="$1"
    local label="$2"
    if command -v "$command_name" >/dev/null 2>&1; then
        pass "${label}: ${command_name}"
        return 0
    fi
    critical "${label}: command not found -> ${command_name}"
    return 1
}

first_token() {
    local csv_path="$1"
    tail -n +2 "$csv_path" | head -n 1 | cut -d',' -f1
}

check_http() {
    local url="$1"
    local expected_status="$2"
    local label="$3"
    local auth_mode="${4:-none}"
    local severity="${5:-warn}"
    local status

    if [ "$auth_mode" = "basic_druid" ]; then
        status=$(curl -s -o /dev/null -w "%{http_code}" -u "admin:admin" "$url" 2>/dev/null || echo "000")
    else
        status=$(curl -s -o /dev/null -w "%{http_code}" "$url" 2>/dev/null || echo "000")
    fi

    if [ "$status" = "$expected_status" ]; then
        pass "${label}: HTTP ${status}"
        return 0
    fi

    if [ "$severity" = "critical" ]; then
        critical "${label}: HTTP ${status}, expected ${expected_status}"
    else
        warn "${label}: HTTP ${status}, expected ${expected_status}"
    fi
    return 1
}

check_metric() {
    local token="$1"
    local metric="$2"
    local status
    status=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${token}" \
        "${BASE_URL}/actuator/metrics/${metric}" 2>/dev/null || echo "000")
    if [ "$status" = "200" ]; then
        pass "Metric available: ${metric}"
        return 0
    fi
    warn "Metric unavailable: ${metric} (HTTP ${status})"
    return 1
}

check_java() {
    if command -v java >/dev/null 2>&1; then
        local java_version
        java_version=$(java -version 2>&1 | head -n 1 || echo "unknown")
        pass "Java available: ${java_version}"
        # Check available memory (approximate via java)
        if command -v java >/dev/null 2>&1; then
            local max_mem
            max_mem=$(java -XX:+PrintFlagsFinal -version 2>&1 | grep 'MaxHeapSize' | awk '{print $4}' || echo "0")
            if [ -n "$max_mem" ] && [ "$max_mem" != "0" ]; then
                local max_mem_gb
                max_mem_gb=$(awk "BEGIN {printf \"%.1f\", ${max_mem} / 1024 / 1024 / 1024}")
                pass "Java max heap: ${max_mem_gb} GB"
            else
                warn "Cannot determine Java max heap size"
            fi
        fi
    else
        warn "Java not found in PATH"
    fi
}

check_python() {
    if command -v python3 >/dev/null 2>&1; then
        pass "Python3 available"
        return 0
    fi
    warn "Python3 not found — result analysis will use awk fallback"
    return 1
}

load_batch_ids() {
    local output
    if ! output=$(bash "${SCRIPT_DIR}/resolve-batch-ids.sh" --format=env 2>&1); then
        warn "Batch resolution failed: ${output}"
        return 1
    fi

    local batch_small=""
    local batch_medium=""
    local batch_large=""
    local batch_query=""
    while IFS='=' read -r key value; do
        case "$key" in
            BATCH_SMALL) batch_small="$value" ;;
            BATCH_MEDIUM) batch_medium="$value" ;;
            BATCH_LARGE) batch_large="$value" ;;
            BATCH_QUERY) batch_query="$value" ;;
        esac
    done <<< "$output"

    if [ -z "$batch_small" ] || [ -z "$batch_medium" ] || [ -z "$batch_large" ] || [ -z "$batch_query" ]; then
        warn "Batch resolution returned incomplete values"
        return 1
    fi

    pass "Resolved batches: PT-SMALL=${batch_small}, PT-MEDIUM=${batch_medium}, PT-LARGE=${batch_large}, PT-QUERY=${batch_query}"
    return 0
}

echo "Checking stress-test prerequisites..."

# OS detection
OS_TYPE="Linux"
case "$(uname -s)" in
    MINGW*|MSYS*|CYGWIN*) OS_TYPE="Windows (Git Bash)" ;;
    Linux) OS_TYPE="Linux" ;;
    Darwin) OS_TYPE="macOS" ;;
esac
pass "OS type: ${OS_TYPE}"

check_file "$ADMIN_TOKEN_CSV" "Admin token csv" || true
check_file "$STUDENT_TOKEN_CSV" "Student token csv" || true
check_command "$JMETER_BIN" "JMeter command" || true
check_command "curl" "curl command" || true
check_java || true
check_python || true

# Health check is WARN severity (not critical)
check_http "${BASE_URL}/actuator/health" "200" "Health check" "none" "warn" || true

# Druid check is WARN severity
check_http "${BASE_URL}/druid/datasource.json" "200" "Druid check" "basic_druid" "warn" || true

admin_token=""
student_token=""
if [ -f "$ADMIN_TOKEN_CSV" ]; then
    admin_token="$(first_token "$ADMIN_TOKEN_CSV")"
fi
if [ -f "$STUDENT_TOKEN_CSV" ]; then
    student_token="$(first_token "$STUDENT_TOKEN_CSV")"
fi

if [ -z "$admin_token" ] || [ -z "$student_token" ]; then
    critical "Token csv is empty"
else
    pass "Token csv has data"
    check_metric "$admin_token" "tomcat.threads.busy" || true
    check_metric "$admin_token" "jvm.gc.pause" || true
    check_metric "$admin_token" "executor.evaluation.active.count" || true
    check_metric "$admin_token" "executor.evaluation.queue.size" || true
    check_metric "$admin_token" "executor.export.active.count" || true
    check_metric "$admin_token" "executor.export.queue.size" || true
fi

load_batch_ids || true

echo ""
echo "======================================"
echo "Preflight summary:"
echo "======================================"
echo "  passed:    ${PASS_COUNT}"
echo "  warned:    ${WARN_COUNT}"
echo "  failed:    ${FAIL_COUNT}"
echo "  critical:  ${CRITICAL_COUNT}"
echo "======================================"

if [ "$CRITICAL_COUNT" -gt 0 ]; then
    echo ""
    echo "CRITICAL issues detected (${CRITICAL_COUNT}). Cannot proceed."
    echo "Fix the CRITICAL items above before running the benchmark."
    exit 1
fi

if [ "$WARN_COUNT" -gt 0 ]; then
    echo ""
    echo "WARNINGS detected (${WARN_COUNT}) but none are critical."
    echo "Benchmark can proceed, but some metrics or checks may be unavailable."
else
    echo "Preflight check passed."
fi
