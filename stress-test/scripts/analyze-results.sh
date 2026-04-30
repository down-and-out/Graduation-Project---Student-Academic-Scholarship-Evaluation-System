#!/bin/bash
# Summarize JMeter JTL files and monitor CSVs into a readable report.

set -euo pipefail

RESULT_DIR="${1:-}"

if [ -z "$RESULT_DIR" ]; then
    echo "Usage: bash analyze-results.sh <result_dir>" >&2
    exit 1
fi

if [ ! -d "$RESULT_DIR" ]; then
    echo "Result directory not found: $RESULT_DIR" >&2
    exit 1
fi

REPORT_MD="${RESULT_DIR}/summary-report.md"
REPORT_JSON="${RESULT_DIR}/summary-report.json"

write_shell_fallback_report() {
    local result_dir="$1"
    local report_md="$2"
    local report_json="$3"
    local tmp_md
    tmp_md="$(mktemp)"

    {
        echo "# Stress Test Summary"
        echo ""
        echo "- Result dir: \`${result_dir}\`"
        echo "- Report mode: shell-fallback"
        echo ""
        echo "## JTL Summary"
        echo ""
        echo "| File | Samples | Error % | 429 % |"
        echo "|------|---------|---------|-------|"
    } > "$tmp_md"

    printf '{\n  "result_dir": "%s",\n  "report_mode": "shell-fallback",\n  "jtl": [\n' "$result_dir" > "$report_json"

    local first_json=1
    local found_jtl=0
    local jtl
    for jtl in "$result_dir"/*.jtl; do
        if [ ! -f "$jtl" ]; then
            continue
        fi
        found_jtl=1
        local file_name total samples errors error_pct code_429 rate_429
        file_name="$(basename "$jtl")"
        total=$(tail -n +2 "$jtl" 2>/dev/null | wc -l | tr -d ' ')
        samples="${total:-0}"
        errors=$(awk -F',' 'NR > 1 && tolower($8) != "true" { count++ } END { print count + 0 }' "$jtl")
        code_429=$(awk -F',' 'NR > 1 && $4 == "429" { count++ } END { print count + 0 }' "$jtl")
        if [ "${samples}" -gt 0 ]; then
            error_pct=$(awk -v e="$errors" -v t="$samples" 'BEGIN { printf "%.2f", (e * 100) / t }')
            rate_429=$(awk -v c="$code_429" -v t="$samples" 'BEGIN { printf "%.2f", (c * 100) / t }')
        else
            error_pct="0.00"
            rate_429="0.00"
        fi

        printf '| %s | %s | %s | %s |\n' "$file_name" "$samples" "$error_pct" "$rate_429" >> "$tmp_md"

        if [ "$first_json" -eq 0 ]; then
            printf ',\n' >> "$report_json"
        fi
        first_json=0
        printf '    {"file":"%s","samples":%s,"error_rate_pct":%s,"http_429_rate_pct":%s}' \
            "$file_name" "$samples" "$error_pct" "$rate_429" >> "$report_json"
    done

    if [ "$found_jtl" -eq 0 ]; then
        echo "| No JTL files found | 0 | 0.00 | 0.00 |" >> "$tmp_md"
    fi

    {
        echo ""
        echo "## Notes"
        echo ""
        echo "- Python3 not available, generated a reduced shell-based summary."
        echo "- Detailed percentiles and monitor aggregation are omitted in fallback mode."
        echo ""
    } >> "$tmp_md"

    printf '\n  ],\n  "monitor": [],\n  "slow_sql_top10": [],\n  "findings": ["Python3 not available, generated a reduced shell-based summary."]\n}\n' >> "$report_json"
    mv "$tmp_md" "$report_md"
    echo "Generated summary: $report_md"
    echo "Generated json: $report_json"
}

if ! command -v python3 >/dev/null 2>&1; then
    write_shell_fallback_report "$RESULT_DIR" "$REPORT_MD" "$REPORT_JSON"
    exit 0
fi

python3 - "$RESULT_DIR" "$REPORT_MD" "$REPORT_JSON" <<'PY'
import csv
import glob
import json
import math
import os
import statistics
import sys
from collections import defaultdict

result_dir, report_md, report_json = sys.argv[1:4]

def percentile(values, pct):
    if not values:
        return None
    ordered = sorted(values)
    if len(ordered) == 1:
        return float(ordered[0])
    pos = (len(ordered) - 1) * pct
    low = math.floor(pos)
    high = math.ceil(pos)
    if low == high:
        return float(ordered[low])
    return float(ordered[low] + (ordered[high] - ordered[low]) * (pos - low))

def parse_number(value, default=0.0):
    try:
        if value in (None, "", "NA", "N/A"):
            return default
        return float(value)
    except Exception:
        return default

def parse_bool(value):
    return str(value).strip().lower() == "true"

def summarize_jtl(path):
    with open(path, "r", encoding="utf-8", newline="") as fh:
        reader = csv.DictReader(fh)
        rows = list(reader)
    if not rows:
        return None
    elapsed = [parse_number(r.get("elapsed"), 0.0) for r in rows]
    success = sum(1 for r in rows if parse_bool(r.get("success")))
    errors = len(rows) - success
    code_429 = sum(1 for r in rows if str(r.get("responseCode", "")).strip() == "429")
    first_ts = min(parse_number(r.get("timeStamp"), 0.0) for r in rows)
    last_ts = max(parse_number(r.get("timeStamp"), 0.0) for r in rows)
    duration_seconds = max((last_ts - first_ts) / 1000.0, 0.001)
    labels = defaultdict(int)
    for row in rows:
        labels[row.get("label", "unknown")] += 1
    return {
        "file": os.path.basename(path),
        "samples": len(rows),
        "success": success,
        "errors": errors,
        "error_rate_pct": round(errors * 100.0 / len(rows), 2),
        "http_429": code_429,
        "http_429_rate_pct": round(code_429 * 100.0 / len(rows), 2),
        "throughput_rps": round(len(rows) / duration_seconds, 2),
        "avg_ms": round(statistics.fmean(elapsed), 2),
        "p50_ms": round(percentile(elapsed, 0.50), 2),
        "p95_ms": round(percentile(elapsed, 0.95), 2),
        "p99_ms": round(percentile(elapsed, 0.99), 2),
        "max_ms": round(max(elapsed), 2),
        "labels": dict(sorted(labels.items())),
    }

def summarize_monitor(path):
    with open(path, "r", encoding="utf-8", newline="") as fh:
        reader = csv.DictReader(fh)
        rows = list(reader)
    if not rows:
        return None
    def col(name):
        return [parse_number(r.get(name), 0.0) for r in rows]
    metrics = {
        "file": os.path.basename(path),
        "druid_active_max": round(max(col("druid_active")), 2),
        "druid_wait_max": round(max(col("druid_wait")), 2),
        "redis_rtt_ms_p95": round(percentile(col("redis_rtt_ms"), 0.95) or 0.0, 2),
        "exec_eval_queue_max": round(max(col("exec_eval_queue")), 2),
        "exec_export_queue_max": round(max(col("exec_export_queue")), 2),
        "tomcat_busy_max": round(max(col("tomcat_busy")), 2),
        "cpu_pct_p95": round(percentile(col("cpu_pct"), 0.95) or 0.0, 2),
        "mem_pct_p95": round(percentile(col("mem_pct"), 0.95) or 0.0, 2),
        "gc_pause_count_max": round(max(col("gc_pause_count")), 2),
    }
    return metrics

def load_slow_sql(path):
    if not os.path.exists(path):
        return []
    try:
        with open(path, "r", encoding="utf-8") as fh:
            raw = json.load(fh)
    except Exception:
        return []
    content = raw.get("Content") or []
    ranked = sorted(content, key=lambda item: item.get("ExecuteTimeMillis", 0), reverse=True)
    top = []
    for item in ranked[:10]:
        count = item.get("ExecuteCount", 0) or 0
        total_ms = float(item.get("ExecuteTimeMillis", 0) or 0)
        avg_ms = total_ms / count if count else 0.0
        sql = " ".join(str(item.get("SQL", "")).split())
        top.append({
            "sql": sql[:160],
            "count": count,
            "total_ms": round(total_ms, 2),
            "avg_ms": round(avg_ms, 2),
        })
    return top

def infer_bottlenecks(jtls, monitors):
    findings = []
    peak_wait = max((m["druid_wait_max"] for m in monitors), default=0)
    peak_active = max((m["druid_active_max"] for m in monitors), default=0)
    peak_redis = max((m["redis_rtt_ms_p95"] for m in monitors), default=0)
    peak_eval_q = max((m["exec_eval_queue_max"] for m in monitors), default=0)
    peak_export_q = max((m["exec_export_queue_max"] for m in monitors), default=0)
    peak_tomcat = max((m["tomcat_busy_max"] for m in monitors), default=0)
    peak_gc = max((m["gc_pause_count_max"] for m in monitors), default=0)
    max_429_rate = max((j["http_429_rate_pct"] for j in jtls), default=0)
    max_p95 = max((j["p95_ms"] for j in jtls), default=0)

    if peak_wait > 0:
        findings.append("数据库连接池可能是瓶颈：`druid_wait_max > 0`。")
    if peak_active >= 45:
        findings.append("数据库连接使用已接近上限：`druid_active_max` 很高，优先结合慢 SQL 排查。")
    if peak_eval_q >= 5:
        findings.append("评定线程池存在明显堆积：`exec_eval_queue_max` 偏高。")
    if peak_export_q >= 3:
        findings.append("导出线程池存在排队：`exec_export_queue_max` 偏高。")
    if peak_redis > 5:
        findings.append("Redis 延迟偏高：`redis_rtt_ms_p95 > 5ms`。")
    if max_429_rate >= 10:
        findings.append("限流命中明显：`429` 占比较高，说明当前结果包含保护机制效应。")
    if peak_tomcat >= 150:
        findings.append("Tomcat 工作线程占用偏高：`tomcat_busy_max` 较高。")
    if peak_gc > 0:
        findings.append("GC 指标出现增长，需结合内存和导出/批处理场景继续观察。")
    if not findings and max_p95 > 2000:
        findings.append("资源未明显打满但 P95 较高，更像业务逻辑或 SQL 本身耗时偏大。")
    if not findings:
        findings.append("未发现明确单点瓶颈，建议结合具体 phase 逐项分析。")
    return findings

jtl_summaries = [x for x in (summarize_jtl(p) for p in sorted(glob.glob(os.path.join(result_dir, "*.jtl")))) if x]
monitor_summaries = [x for x in (summarize_monitor(p) for p in sorted(glob.glob(os.path.join(result_dir, "monitor_*.csv")))) if x]
slow_sql_top = load_slow_sql(os.path.join(result_dir, "slow-sql.json"))
findings = infer_bottlenecks(jtl_summaries, monitor_summaries)

payload = {
    "result_dir": result_dir,
    "jtl": jtl_summaries,
    "monitor": monitor_summaries,
    "slow_sql_top10": slow_sql_top,
    "findings": findings,
}
with open(report_json, "w", encoding="utf-8") as fh:
    json.dump(payload, fh, ensure_ascii=False, indent=2)

lines = []
lines.append("# Stress Test Summary")
lines.append("")
lines.append(f"- Result dir: `{result_dir}`")
lines.append("")
lines.append("## Bottleneck Hints")
lines.append("")
for finding in findings:
    lines.append(f"- {finding}")
lines.append("")
lines.append("## JTL Summary")
lines.append("")
lines.append("| File | Samples | Error % | 429 % | RPS | Avg ms | P95 ms | P99 ms | Max ms |")
lines.append("|------|---------|---------|-------|-----|--------|--------|--------|--------|")
for item in jtl_summaries:
    lines.append(f"| {item['file']} | {item['samples']} | {item['error_rate_pct']} | {item['http_429_rate_pct']} | {item['throughput_rps']} | {item['avg_ms']} | {item['p95_ms']} | {item['p99_ms']} | {item['max_ms']} |")
lines.append("")
lines.append("## Monitor Summary")
lines.append("")
lines.append("| File | druid_active_max | druid_wait_max | redis_rtt_p95 | eval_queue_max | export_queue_max | tomcat_busy_max | cpu_p95 | mem_p95 |")
lines.append("|------|------------------|----------------|---------------|----------------|------------------|-----------------|---------|---------|")
for item in monitor_summaries:
    lines.append(f"| {item['file']} | {item['druid_active_max']} | {item['druid_wait_max']} | {item['redis_rtt_ms_p95']} | {item['exec_eval_queue_max']} | {item['exec_export_queue_max']} | {item['tomcat_busy_max']} | {item['cpu_pct_p95']} | {item['mem_pct_p95']} |")
lines.append("")
lines.append("## Slow SQL Top 10")
lines.append("")
if slow_sql_top:
    lines.append("| Avg ms | Count | Total ms | SQL |")
    lines.append("|--------|-------|----------|-----|")
    for item in slow_sql_top:
        lines.append(f"| {item['avg_ms']} | {item['count']} | {item['total_ms']} | `{item['sql']}` |")
else:
    lines.append("No slow SQL statistics available.")
lines.append("")

with open(report_md, "w", encoding="utf-8") as fh:
    fh.write("\n".join(lines))

print(f"Generated summary: {report_md}")
print(f"Generated json: {report_json}")
PY
