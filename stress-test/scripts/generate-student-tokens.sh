#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
STRESS_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
DEFAULT_OUTPUT="${STRESS_ROOT}/data/tokens-student.csv"

exec bash "${SCRIPT_DIR}/generate-tokens.sh" student "${1:-200}" "${2:-$DEFAULT_OUTPUT}"
