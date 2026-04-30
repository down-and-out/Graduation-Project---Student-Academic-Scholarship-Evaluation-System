#!/bin/bash
# Resolve PT batch ids and print shell assignments.

set -euo pipefail

FORMAT="shell"
if [ "${1:-}" = "--format=env" ]; then
    FORMAT="env"
elif [ -n "${1:-}" ]; then
    echo "Unsupported argument: $1" >&2
    exit 1
fi

MYSQL_HOST="${MYSQL_HOST:-127.0.0.1}"
MYSQL_PORT="${MYSQL_PORT:-3306}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-}"
MYSQL_DB="${MYSQL_DB:-scholarship}"

MYSQL_ARGS=(-h "$MYSQL_HOST" -P "$MYSQL_PORT" -u "$MYSQL_USER" --batch --raw --skip-column-names)
if [ -n "$MYSQL_PASSWORD" ]; then
    MYSQL_ARGS+=(-p"$MYSQL_PASSWORD")
fi

query_result=$(mysql "${MYSQL_ARGS[@]}" "$MYSQL_DB" -e "
SELECT batch_code, id
FROM evaluation_batch
WHERE batch_code IN ('PT-SMALL', 'PT-MEDIUM', 'PT-LARGE', 'PT-QUERY');
")

batch_small=""
batch_medium=""
batch_large=""
batch_query=""

while IFS=$'\t' read -r batch_code batch_id; do
    case "$batch_code" in
        PT-SMALL) batch_small="$batch_id" ;;
        PT-MEDIUM) batch_medium="$batch_id" ;;
        PT-LARGE) batch_large="$batch_id" ;;
        PT-QUERY) batch_query="$batch_id" ;;
    esac
done <<< "$query_result"

if [ -z "$batch_small" ] || [ -z "$batch_medium" ] || [ -z "$batch_large" ] || [ -z "$batch_query" ]; then
    echo "Missing PT batch ids in evaluation_batch" >&2
    exit 1
fi

if [ "$FORMAT" = "shell" ] || [ "$FORMAT" = "env" ]; then
cat <<EOF
BATCH_SMALL=${batch_small}
BATCH_MEDIUM=${batch_medium}
BATCH_LARGE=${batch_large}
BATCH_QUERY=${batch_query}
EOF
fi
