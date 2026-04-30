#!/bin/bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
exec bash "${SCRIPT_DIR}/generate-tokens.sh" admin "${1:-40}" "${2:-../data/tokens-admin.csv}"
