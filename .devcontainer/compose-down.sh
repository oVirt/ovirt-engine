#!/usr/bin/env bash
# Bring down a devcontainer stack for a specific variant.
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(cd "${SCRIPT_DIR}/.." && pwd)"
if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <variant-dir-name>" >&2
  exit 1
fi
VARIANT="$1"
OVERRIDE_FILE="${SCRIPT_DIR}/${VARIANT}/docker-compose.override.yml"
BASE_FILE="${ROOT_DIR}/docker-compose.yml"
if [[ ! -f "${OVERRIDE_FILE}" ]]; then
  echo "Error: override file not found: ${OVERRIDE_FILE}" >&2
  exit 2
fi
if [[ ! -f "${BASE_FILE}" ]]; then
  echo "Error: base compose file not found: ${BASE_FILE}" >&2
  exit 3
fi
set -x
docker compose -f "${BASE_FILE}" -f "${OVERRIDE_FILE}" down --remove-orphans
