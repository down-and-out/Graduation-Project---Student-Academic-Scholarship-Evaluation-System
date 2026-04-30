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

scan_delete() {
    local pattern="$1"
    local count=0
    while IFS= read -r key; do
        [ -z "$key" ] && continue
        redis-cli "${REDIS_ARGS[@]}" DEL "$key" >/dev/null
        count=$((count + 1))
    done < <(redis-cli "${REDIS_ARGS[@]}" --scan --pattern "$pattern")
    echo "$count"
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
    "scholarship:app:*"
    "scholarship:eval:*"
    "scholarship:batch:*"
    "scholarship:rule:*"
    "scholarship:sys-setting:*"
    "token:blacklist:*"
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
