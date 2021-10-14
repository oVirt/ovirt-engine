#!/bin/sh -x

# Search for playbooks within specified directories (one level only)
PLABOOKS_DIR="packaging/ansible-runner-service-project/project"

# Directory with roles
ROLES_DIR="packaging/ansible-runner-service-project/project/roles"

SRCDIR="$(dirname "$0")/.."

ANSIBLE_LINT=/usr/bin/ansible-lint
ANSIBLE_LINT_CONF="$(dirname "$0")/ansible-lint.conf"

if ! which "${ANSIBLE_LINT}" > /dev/null 2>&1; then
	echo "WARNING: tool '${ANSIBLE_LINT}' is missing" >&2
	exit 0
fi

cd "${SRCDIR}"

# Find playbooks
PARAMS=$(find ${PLABOOKS_DIR} -type f -name '*.yml' -maxdepth 1)

# Find roles
PARAMS="$PARAMS $(find ${ROLES_DIR} -type d -maxdepth 1)"

${ANSIBLE_LINT} -c ${ANSIBLE_LINT_CONF} ${PARAMS}
