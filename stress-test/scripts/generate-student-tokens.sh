#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
exec bash "${SCRIPT_DIR}/generate-tokens.sh" student "${1:-200}" "${2:-../data/tokens-student.csv}"
