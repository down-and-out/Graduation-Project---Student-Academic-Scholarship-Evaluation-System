#!/bin/bash
# 一键清理压测数据：Redis → MySQL
# Usage:
#   bash stress-test/scripts/cleanup-all.sh
# 跳过某一步：
#   SKIP_REDIS=1 bash stress-test/scripts/cleanup-all.sh
#   SKIP_MYSQL=1 bash stress-test/scripts/cleanup-all.sh
#
# 环境变量（与子脚本一致）：
#   REDIS_HOST / REDIS_PORT / REDIS_PASSWORD / REDIS_DB
#   MYSQL_HOST / MYSQL_PORT / MYSQL_USER / MYSQL_PASSWORD / MYSQL_DATABASE

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

echo "=============================================="
echo "  Scholarship 压测数据清理"
echo "=============================================="

# ---------- Redis ----------
if [ "${SKIP_REDIS:-0}" = "1" ]; then
    echo ""
    echo "[Redis] SKIP_REDIS=1, skip"
else
    echo ""
    echo ">>> [Redis] 开始清理缓存 key ..."
    bash "$SCRIPT_DIR/cleanup-redis.sh"
    echo "<<< [Redis] 清理完成"
fi

# ---------- MySQL ----------
if [ "${SKIP_MYSQL:-0}" = "1" ]; then
    echo ""
    echo "[MySQL] SKIP_MYSQL=1, skip"
else
    echo ""
    echo ">>> [MySQL] 开始清理压测数据 ..."

    MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
    MYSQL_PORT="${MYSQL_PORT:-3306}"
    MYSQL_USER="${MYSQL_USER:-root}"
    MYSQL_PASSWORD="${MYSQL_PASSWORD:-123456}"
    MYSQL_DATABASE="${MYSQL_DATABASE:-scholarship}"

    mysql -h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" -p"$MYSQL_PASSWORD" "$MYSQL_DATABASE" < "$SCRIPT_DIR/cleanup-data.sql"
    echo "<<< [MySQL] 清理完成"
fi

echo ""
echo "=============================================="
echo "  清理流程结束"
echo "=============================================="
