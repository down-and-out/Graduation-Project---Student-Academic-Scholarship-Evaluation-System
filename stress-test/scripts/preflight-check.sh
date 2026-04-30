#!/bin/bash
# Validate local stress-test prerequisites.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BASE_URL="${BASE_URL:-http://localhost:8080/api}"
ADMIN_TOKEN_CSV="${ADMIN_TOKEN_CSV:-${SCRIPT_DIR}/../data/tokens-admin.csv}"
STUDENT_TOKEN_CSV="${STUDENT_TOKEN_CSV:-${SCRIPT_DIR}/../data/tokens-student.csv}"
JMETER_BIN="${JMETER_BIN:-jmeter}"

require_file() {
    local path="$1"
    if [ ! -f "$path" ]; then
        echo "Missing file: $path" >&2
        exit 1
    fi
}

first_token() {
    local csv_path="$1"
    tail -n +2 "$csv_path" | head -n 1 | cut -d',' -f1
}

check_metric() {
    local token="$1"
    local metric="$2"
    local status
    status=$(curl -s -o /dev/null -w "%{http_code}" -H "Authorization: Bearer ${token}" \
        "${BASE_URL}/actuator/metrics/${metric}" 2>/dev/null || echo "000")
    if [ "$status" != "200" ]; then
        echo "Metric unavailable: ${metric} (HTTP ${status})" >&2
        exit 1
    fi
}

echo "Checking stress-test prerequisites..."

require_file "$ADMIN_TOKEN_CSV"
require_file "$STUDENT_TOKEN_CSV"

if ! command -v "$JMETER_BIN" >/dev/null 2>&1; then
    echo "JMeter command not found: $JMETER_BIN" >&2
    exit 1
fi

health_status=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health" 2>/dev/null || echo "000")
if [ "$health_status" != "200" ]; then
    echo "Health check failed: ${BASE_URL}/actuator/health -> ${health_status}" >&2
    exit 1
fi

druid_status=$(curl -s -o /dev/null -w "%{http_code}" -u "admin:admin" "${BASE_URL}/druid/datasource.json" 2>/dev/null || echo "000")
if [ "$druid_status" != "200" ]; then
    echo "Druid check failed: ${BASE_URL}/druid/datasource.json -> ${druid_status}" >&2
    exit 1
fi

admin_token="$(first_token "$ADMIN_TOKEN_CSV")"
student_token="$(first_token "$STUDENT_TOKEN_CSV")"

if [ -z "$admin_token" ] || [ -z "$student_token" ]; then
    echo "Token csv is empty." >&2
    exit 1
fi

check_metric "$admin_token" "tomcat.threads.busy"
check_metric "$admin_token" "jvm.gc.pause"
check_metric "$admin_token" "executor.evaluation.active.count"
check_metric "$admin_token" "executor.evaluation.queue.size"
check_metric "$admin_token" "executor.export.active.count"
check_metric "$admin_token" "executor.export.queue.size"

source <(bash "${SCRIPT_DIR}/resolve-batch-ids.sh")

echo "Resolved batches:"
echo "  PT-SMALL=${BATCH_SMALL}"
echo "  PT-MEDIUM=${BATCH_MEDIUM}"
echo "  PT-LARGE=${BATCH_LARGE}"
echo "  PT-QUERY=${BATCH_QUERY}"
echo "Preflight check passed."
