#!/bin/bash
# Collect runtime metrics into CSV during stress tests.

set -euo pipefail

TOKEN="${1:-}"
OUTPUT_CSV="${2:-../results/monitor-$(date +%Y%m%d_%H%M%S).csv}"
INTERVAL="${3:-1}"
BASE_URL="${BASE_URL:-http://localhost:8080/api}"
PHASE_NAME="${PHASE_NAME:-adhoc}"
JAVA_PID="${JAVA_PID:-$(jps -l 2>/dev/null | grep scholarship | awk '{print $1}' || echo '')}"

if [ -z "$TOKEN" ]; then
    echo "Usage: bash monitor.sh <token> [output_csv] [interval_seconds]" >&2
    exit 1
fi

mkdir -p "$(dirname "$OUTPUT_CSV")"
echo "phase,timestamp,druid_active,druid_wait,redis_rtt_ms,exec_eval_active,exec_eval_queue,exec_eval_pool,exec_eval_completed,exec_export_active,exec_export_queue,tomcat_busy,cpu_pct,mem_pct,gc_pause_count" > "$OUTPUT_CSV"

AUTH="Authorization: Bearer $TOKEN"
DRUID_AUTH="admin:admin"

fetch_metric() {
    local metric="$1"
    curl -s --max-time 3 -H "$AUTH" "${BASE_URL}/actuator/metrics/${metric}" 2>/dev/null \
        | grep -o '"value":[0-9.]*' | head -n 1 | sed 's/"value"://'
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

while true; do
    ts=$(date +%Y-%m-%dT%H:%M:%S)

    druid_resp=$(curl -s --max-time 3 -u "$DRUID_AUTH" "${BASE_URL}/druid/datasource.json" 2>/dev/null || echo '{}')
    druid_active=$(echo "$druid_resp" | grep -o '"ActiveCount":[0-9]*' | head -n 1 | sed 's/"ActiveCount"://' || true)
    druid_wait=$(echo "$druid_resp" | grep -o '"WaitThreadCount":[0-9]*' | head -n 1 | sed 's/"WaitThreadCount"://' || true)
    druid_active="${druid_active:-0}"
    druid_wait="${druid_wait:-0}"

    redis_rtt=0
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

    exec_eval_active="${exec_eval_active:-0}"
    exec_eval_queue="${exec_eval_queue:-0}"
    exec_eval_pool="${exec_eval_pool:-0}"
    exec_eval_completed="${exec_eval_completed:-0}"
    exec_export_active="${exec_export_active:-0}"
    exec_export_queue="${exec_export_queue:-0}"
    tomcat_busy="${tomcat_busy:-0}"
    gc_pause_count="${gc_pause_count:-0}"

    cpu_pct=0
    mem_pct=0
    if [ -n "$JAVA_PID" ]; then
        cpu_pct=$(ps -p "$JAVA_PID" -o %cpu= 2>/dev/null | tr -d ' ' || echo "0")
        mem_pct=$(ps -p "$JAVA_PID" -o %mem= 2>/dev/null | tr -d ' ' || echo "0")
    fi

    echo "${PHASE_NAME},${ts},${druid_active},${druid_wait},${redis_rtt},${exec_eval_active},${exec_eval_queue},${exec_eval_pool},${exec_eval_completed},${exec_export_active},${exec_export_queue},${tomcat_busy},${cpu_pct:-0},${mem_pct:-0},${gc_pause_count}" >> "$OUTPUT_CSV"
    sleep "$INTERVAL"
done
