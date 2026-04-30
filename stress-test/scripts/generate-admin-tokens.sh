#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STRESS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
DEFAULT_OUTPUT="${STRESS_ROOT}/data/tokens-admin.csv"

exec bash "${SCRIPT_DIR}/generate-tokens.sh" admin "${1:-40}" "${2:-$DEFAULT_OUTPUT}"
