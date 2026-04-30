#!/bin/bash
# ========================================
# 奖学金评审系统 — 慢 SQL 统计导出脚本
# 用途：压测结束后从 Druid 监控中导出慢 SQL 统计
# 使用方式：bash slow-sql-monitor.sh [输出文件]
# ========================================

BASE_URL="${BASE_URL:-http://localhost:8080/api}"
OUTPUT_FILE="${1:-../results/slow-sql-$(date +%Y%m%d_%H%M%S).json}"
DRUID_AUTH="admin:admin"

mkdir -p "$(dirname "$OUTPUT_FILE")"

echo ">>> 从 Druid /druid/sql.json 导出慢 SQL 统计..."
echo "API 地址: $BASE_URL"
echo "输出文件: $OUTPUT_FILE"

# 获取 SQL 统计
SQL_STATS=$(curl -s --max-time 10 -u "$DRUID_AUTH" "${BASE_URL}/druid/sql.json" 2>/dev/null || echo '{}')

# 保存原始数据
echo "$SQL_STATS" > "$OUTPUT_FILE"

# 提取关键信息
echo ""
echo "======================================"
echo "  SQL 执行统计摘要"
echo "======================================"

# 检查是否有数据
if echo "$SQL_STATS" | grep -q '"Content"'; then
    echo "$SQL_STATS" | python3 -c "
import json, sys
data = json.load(sys.stdin)
content = data.get('Content', [])
if content:
    sorted_sql = sorted(content, key=lambda x: x.get('ExecuteTimeMillis', 0), reverse=True)[:20]
    print(f'{"SQL":<50s} {\"执行次数\":>8s} {\"执行时间(ms)\":>14s} {\"平均(ms)\":>10s}')
    print('-' * 85)
    for item in sorted_sql:
        sql = item.get('SQL', '')[:48]
        cnt = item.get('ExecuteCount', 0)
        total = item.get('ExecuteTimeMillis', 0)
        avg = total / cnt if cnt > 0 else 0
        print(f'{sql:<50s} {cnt:>8d} {total:>14.2f} {avg:>10.2f}')
else:
    print('无 SQL 统计数据')
" 2>/dev/null || echo "Python3 不可用，原始数据已保存到 $OUTPUT_FILE"
else
    echo "无 SQL 统计数据，原始响应已保存到 $OUTPUT_FILE"
fi

echo ""
echo "完整数据: $OUTPUT_FILE"
