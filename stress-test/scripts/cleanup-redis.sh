#!/bin/bash
# Cleanup Redis keys created or amplified by stress-test runs.
# Usage:
#   bash stress-test/scripts/cleanup-redis.sh
# Optional env:
#   REDIS_HOST=127.0.0.1
#   REDIS_PORT=6379
#   REDIS_PASSWORD=xxxx
#   REDIS_DB=0

set -euo pipefail

REDIS_HOST="${REDIS_HOST:-127.0.0.1}"
REDIS_PORT="${REDIS_PORT:-6379}"
REDIS_PASSWORD="${REDIS_PASSWORD:-}"
REDIS_DB="${REDIS_DB:-0}"

REDIS_ARGS=(-h "$REDIS_HOST" -p "$REDIS_PORT" -n "$REDIS_DB" --raw)
if [ -n "$REDIS_PASSWORD" ]; then
    REDIS_ARGS+=(-a "$REDIS_PASSWORD" --no-auth-warning)
fi

if ! command -v redis-cli >/dev/null 2>&1; then
    echo "redis-cli not found" >&2
    exit 1
fi

# Lua script: server-side SCAN + batch DEL to work around
# redis-cli --scan returning empty results in some environments
read -r -d '' SCAN_DEL_LUA << 'LUA_EOF'
local cursor = "0"
local total = 0
repeat
    local result = redis.call("SCAN", cursor, "MATCH", KEYS[1], "COUNT", 1000)
    cursor = result[1]
    local keys = result[2]
    if #keys > 0 then
        total = total + redis.call("DEL", unpack(keys))
    end
until cursor == "0"
return total
LUA_EOF

scan_delete() {
    local pattern="$1"
    redis-cli "${REDIS_ARGS[@]}" EVAL "$SCAN_DEL_LUA" 1 "$pattern"
}

echo "======================================"
echo "Redis cleanup for stress-test"
echo "======================================"
echo "host: $REDIS_HOST"
echo "port: $REDIS_PORT"
echo "db:   $REDIS_DB"
echo "======================================"

declare -a PATTERNS=(
    "rate-limit:*"
    "login:attempt:*"
    "login:lock:*"
    "app:no:counter:*"
    "lock:application:submit:*"
    "lock:review:application:*"
    "lock:evaluation:task:create:*"
    "lock:evaluation:task:execute:*"
    "lock:evaluation:batch:*"
    "token:blacklist:*"
    # Spring Cache 实际 key 前缀（替代之前错误的 scholarship:* 前缀）
    "app:detail::*"
    "app:achievements::*"
    "app:page::*"
    "eval:student::*"
    "eval:page::*"
    "eval:admin::*"
    "eval:rank::*"
    "batch:available::*"
    "batch:detail::*"
    "rule:available::*"
    "rule:detail::*"
    "sys:settings::*"
    "sys:settings:active::*"
    "sys:settings:all::*"
    "task:detail::*"
)

total_deleted=0
for pattern in "${PATTERNS[@]}"; do
    deleted=$(scan_delete "$pattern")
    total_deleted=$((total_deleted + deleted))
    printf '%-40s %s\n' "$pattern" "$deleted"
done

echo "--------------------------------------"
echo "total deleted: $total_deleted"
echo "done"
