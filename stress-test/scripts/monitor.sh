#!/bin/bash
# Collect runtime metrics into CSV during stress tests.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STRESS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

TOKEN="${1:-}"
OUTPUT_CSV="${2:-${STRESS_ROOT}/results/monitor-$(date +%Y%m%d_%H%M%S).csv}"
INTERVAL="${3:-1}"
BASE_URL="${BASE_URL:-http://localhost:8080/api}"
PHASE_NAME="${PHASE_NAME:-adhoc}"
JAVA_PID="${JAVA_PID:-}"

resolve_path() {
    local path="$1"
    case "$path" in
        [A-Za-z]:/*|[A-Za-z]:\\*|/*|\\\\*)
            printf '%s\n' "$path"
            ;;
        *)
            printf '%s/%s\n' "$(pwd)" "$path"
            ;;
    esac
}

if [ -z "$TOKEN" ]; then
    echo "Usage: bash monitor.sh <token> [output_csv] [interval_seconds]" >&2
    exit 1
fi

OUTPUT_CSV="$(resolve_path "$OUTPUT_CSV")"
mkdir -p "$(dirname "$OUTPUT_CSV")"
echo "phase,timestamp,druid_active,druid_wait,redis_rtt_ms,exec_eval_active,exec_eval_queue,exec_eval_pool,exec_eval_completed,exec_export_active,exec_export_queue,tomcat_busy,cpu_pct,mem_pct,gc_pause_count" > "$OUTPUT_CSV"

AUTH="Authorization: Bearer $TOKEN"
DRUID_AUTH="admin:admin"

detect_java_pid() {
    if [ -n "${JAVA_PID:-}" ]; then
        printf '%s\n' "$JAVA_PID"
        return 0
    fi
    if command -v jps >/dev/null 2>&1; then
        jps -l 2>/dev/null | grep scholarship | awk '{print $1}' || true
        return 0
    fi
    return 0
}

supports_ps_metrics() {
    command -v ps >/dev/null 2>&1
}

metric_value_or_na() {
    local value="$1"
    if [ -n "$value" ]; then
        printf '%s\n' "$value"
    else
        printf 'NA\n'
    fi
}

fetch_metric() {
    local metric="$1"
    local payload
    payload=$(curl -s --max-time 3 -H "$AUTH" "${BASE_URL}/actuator/metrics/${metric}" 2>/dev/null || true)
    if [ -z "$payload" ]; then
        printf 'NA\n'
        return 0
    fi
    local value
    value=$(printf '%s' "$payload" | grep -o '"value":[0-9.]*' | head -n 1 | sed 's/"value"://' || true)
    metric_value_or_na "$value"
}

probe_metric() {
    local metric="$1"
    local status
    status=$(curl -s -o /dev/null -w "%{http_code}" -H "$AUTH" "${BASE_URL}/actuator/metrics/${metric}" 2>/dev/null || echo "000")
    if [ "$status" != "200" ]; then
        echo "Warning: metric unavailable -> ${metric} (${status})" >&2
    fi
}

probe_metric "executor.evaluation.active.count"
probe_metric "executor.evaluation.queue.size"
probe_metric "executor.export.active.count"
probe_metric "executor.export.queue.size"
probe_metric "tomcat.threads.busy"
probe_metric "jvm.gc.pause"

JAVA_PID="$(detect_java_pid)"
if [ -z "$JAVA_PID" ]; then
    echo "Warning: Java PID not detected, cpu/mem metrics will be NA" >&2
fi
if ! supports_ps_metrics; then
    echo "Warning: ps command not available, cpu/mem metrics will be NA" >&2
fi
if ! command -v redis-cli >/dev/null 2>&1; then
    echo "Warning: redis-cli not available, redis_rtt_ms will be NA" >&2
fi

while true; do
    ts=$(date +%Y-%m-%dT%H:%M:%S)

    druid_resp=$(curl -s --max-time 3 -u "$DRUID_AUTH" "${BASE_URL}/druid/datasource.json" 2>/dev/null || echo '{}')
    druid_active=$(echo "$druid_resp" | grep -o '"ActiveCount":[0-9]*' | head -n 1 | sed 's/"ActiveCount"://' || true)
    druid_wait=$(echo "$druid_resp" | grep -o '"WaitThreadCount":[0-9]*' | head -n 1 | sed 's/"WaitThreadCount"://' || true)
    druid_active="$(metric_value_or_na "$druid_active")"
    druid_wait="$(metric_value_or_na "$druid_wait")"

    redis_rtt="NA"
    if command -v redis-cli >/dev/null 2>&1; then
        redis_start=$(date +%s%N)
        if redis-cli --no-auth-warning PING > /dev/null 2>&1; then
            redis_end=$(date +%s%N)
            redis_rtt=$(( (redis_end - redis_start) / 1000000 ))
        fi
    fi

    exec_eval_active="$(fetch_metric "executor.evaluation.active.count")"
    exec_eval_queue="$(fetch_metric "executor.evaluation.queue.size")"
    exec_eval_pool="$(fetch_metric "executor.evaluation.pool.size")"
    exec_eval_completed="$(fetch_metric "executor.evaluation.completed.tasks")"
    exec_export_active="$(fetch_metric "executor.export.active.count")"
    exec_export_queue="$(fetch_metric "executor.export.queue.size")"
    tomcat_busy="$(fetch_metric "tomcat.threads.busy")"
    gc_pause_count="$(fetch_metric "jvm.gc.pause")"

    exec_eval_active="$(metric_value_or_na "$exec_eval_active")"
    exec_eval_queue="$(metric_value_or_na "$exec_eval_queue")"
    exec_eval_pool="$(metric_value_or_na "$exec_eval_pool")"
    exec_eval_completed="$(metric_value_or_na "$exec_eval_completed")"
    exec_export_active="$(metric_value_or_na "$exec_export_active")"
    exec_export_queue="$(metric_value_or_na "$exec_export_queue")"
    tomcat_busy="$(metric_value_or_na "$tomcat_busy")"
    gc_pause_count="$(metric_value_or_na "$gc_pause_count")"

    cpu_pct="NA"
    mem_pct="NA"
    if [ -n "$JAVA_PID" ] && supports_ps_metrics; then
        cpu_pct=$(ps -p "$JAVA_PID" -o %cpu= 2>/dev/null | tr -d ' ' || true)
        mem_pct=$(ps -p "$JAVA_PID" -o %mem= 2>/dev/null | tr -d ' ' || true)
        cpu_pct="$(metric_value_or_na "$cpu_pct")"
        mem_pct="$(metric_value_or_na "$mem_pct")"
    fi

    echo "${PHASE_NAME},${ts},${druid_active},${druid_wait},${redis_rtt},${exec_eval_active},${exec_eval_queue},${exec_eval_pool},${exec_eval_completed},${exec_export_active},${exec_export_queue},${tomcat_busy},${cpu_pct},${mem_pct},${gc_pause_count}" >> "$OUTPUT_CSV"
    sleep "$INTERVAL"
done
