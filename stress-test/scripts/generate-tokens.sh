#!/bin/bash
# Generate JWT tokens for stress tests.
# Usage:
#   bash generate-tokens.sh admin [count] [output_csv]
#   bash generate-tokens.sh student [count] [output_csv]

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STRESS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
DATA_ROOT="${STRESS_ROOT}/data"

ROLE_TYPE="${1:-}"
TOKEN_COUNT="${2:-}"
OUTPUT_CSV="${3:-}"

BASE_URL="${BASE_URL:-http://localhost:8080/api}"
ADMIN_USERNAME="${ADMIN_USERNAME:-admin}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-a123456789}"
STUDENT_USERNAME_PREFIX="${STUDENT_USERNAME_PREFIX:-pt_test_}"
STUDENT_PASSWORD="${STUDENT_PASSWORD:-a123456789}"
LOGIN_INTERVAL_EVERY="${LOGIN_INTERVAL_EVERY:-50}"
LOGIN_INTERVAL_SLEEP="${LOGIN_INTERVAL_SLEEP:-0.2}"
FAILURE_CSV="${FAILURE_CSV:-}"

if [ -z "$ROLE_TYPE" ]; then
    echo "Usage: bash generate-tokens.sh <admin|student> [count] [output_csv]"
    exit 1
fi

case "$ROLE_TYPE" in
    admin)
        TOKEN_COUNT="${TOKEN_COUNT:-40}"
        OUTPUT_CSV="${OUTPUT_CSV:-${DATA_ROOT}/tokens-admin.csv}"
        ;;
    student)
        TOKEN_COUNT="${TOKEN_COUNT:-200}"
        OUTPUT_CSV="${OUTPUT_CSV:-${DATA_ROOT}/tokens-student.csv}"
        ;;
    *)
        echo "Unsupported role type: $ROLE_TYPE"
        exit 1
        ;;
esac

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

OUTPUT_CSV="$(resolve_path "$OUTPUT_CSV")"
mkdir -p "$(dirname "$OUTPUT_CSV")"
if [ -z "$FAILURE_CSV" ]; then
    FAILURE_CSV="$(dirname "$OUTPUT_CSV")/$(basename "$OUTPUT_CSV" .csv)-failures.csv"
fi
FAILURE_CSV="$(resolve_path "$FAILURE_CSV")"
mkdir -p "$(dirname "$FAILURE_CSV")"

echo "======================================"
echo "Generate tokens"
echo "======================================"
echo "role:        $ROLE_TYPE"
echo "base url:    $BASE_URL"
echo "count:       $TOKEN_COUNT"
echo "output:      $OUTPUT_CSV"
echo "failures:    $FAILURE_CSV"
echo "======================================"

HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${BASE_URL}/actuator/health" 2>/dev/null || echo "000")
if [ "$HTTP_CODE" != "200" ]; then
    echo "Warning: /actuator/health returned $HTTP_CODE"
fi

echo "token,username,user_id,user_type" > "$OUTPUT_CSV"
echo "username,http_code,error_message" > "$FAILURE_CSV"

extract_json_string() {
    local payload="$1"
    local key="$2"
    if command -v jq >/dev/null 2>&1; then
        printf '%s' "$payload" | jq -r --arg key "$key" '.[$key] // empty' 2>/dev/null | head -n 1
        return 0
    fi
    printf '%s' "$payload" | sed -n "s/.*\"$key\":\"\\([^\"]*\\)\".*/\\1/p" | head -n 1
}

extract_json_number() {
    local payload="$1"
    local key="$2"
    if command -v jq >/dev/null 2>&1; then
        printf '%s' "$payload" | jq -r --arg key "$key" '.[$key] // empty | tostring' 2>/dev/null | head -n 1
        return 0
    fi
    printf '%s' "$payload" | sed -n "s/.*\"$key\":\"\{0,1\}\\([0-9][0-9]*\\)\"\{0,1\}.*/\\1/p" | head -n 1
}

record_failure() {
    local username="$1"
    local http_code="$2"
    local error_message="$3"
    local sanitized_message
    sanitized_message=$(printf '%s' "$error_message" | tr '\r\n' ' ' | sed 's/"/""/g')
    printf '%s,%s,"%s"\n' "$username" "$http_code" "$sanitized_message" >> "$FAILURE_CSV"
}

login_and_write() {
    local username="$1"
    local password="$2"

    local response http_code
    response=$(curl -s -w $'\n%{http_code}' -X POST "${BASE_URL}/auth/login" \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"${username}\",\"password\":\"${password}\"}")
    http_code="$(printf '%s' "$response" | tail -n 1)"
    response="$(printf '%s' "$response" | sed '$d')"

    local token user_id user_type
    token=$(extract_json_string "$response" "token")
    user_id=$(extract_json_number "$response" "userId")
    user_type=$(extract_json_number "$response" "userType")

    if [ "$http_code" != "200" ] || [ -z "$token" ] || [ -z "$user_id" ] || [ -z "$user_type" ]; then
        record_failure "$username" "$http_code" "$response"
        echo "Login failed for ${username}: $response" >&2
        return 1
    fi

    echo "${token},${username},${user_id},${user_type}" >> "$OUTPUT_CSV"
    return 0
}

success_count=0
failure_count=0

if [ "$ROLE_TYPE" = "admin" ]; then
    while [ "$success_count" -lt "$TOKEN_COUNT" ]; do
        if login_and_write "$ADMIN_USERNAME" "$ADMIN_PASSWORD"; then
            success_count=$((success_count + 1))
        else
            failure_count=$((failure_count + 1))
        fi

        if [ $((success_count % 10)) -eq 0 ] && [ "$success_count" -gt 0 ]; then
            echo "generated admin tokens: $success_count / $TOKEN_COUNT"
        fi

        if [ $(((success_count + failure_count) % LOGIN_INTERVAL_EVERY)) -eq 0 ]; then
            sleep "$LOGIN_INTERVAL_SLEEP"
        fi

        if [ "$failure_count" -ge 5 ]; then
            echo "Too many admin login failures, aborting."
            exit 1
        fi
    done
else
    i=0
    while [ "$success_count" -lt "$TOKEN_COUNT" ]; do
        username=$(printf "%s%04d" "$STUDENT_USERNAME_PREFIX" "$i")
        if login_and_write "$username" "$STUDENT_PASSWORD"; then
            success_count=$((success_count + 1))
        else
            failure_count=$((failure_count + 1))
        fi

        if [ $((success_count % 25)) -eq 0 ] && [ "$success_count" -gt 0 ]; then
            echo "generated student tokens: $success_count / $TOKEN_COUNT"
        fi

        if [ $(((success_count + failure_count) % LOGIN_INTERVAL_EVERY)) -eq 0 ]; then
            sleep "$LOGIN_INTERVAL_SLEEP"
        fi

        i=$((i + 1))
        if [ "$i" -ge 5000 ]; then
            echo "Reached username scan limit before collecting enough student tokens."
            exit 1
        fi
    done
fi

echo "done: success=${success_count}, failures=${failure_count}"
