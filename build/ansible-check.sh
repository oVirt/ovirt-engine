#!/bin/sh -x

ANSIBLE_LINT_CONF="$(dirname "$0")/ansible-lint.conf"

# Check if the ansible-lint binary exists
if ! command -v ansible-lint > /dev/null 2>&1; then
	echo "WARNING: tool 'ansible-lint' is missing" >&2
	exit 0
fi

# Run ansible-lint
ansible-lint -c ${ANSIBLE_LINT_CONF} packaging/ansible-runner-service-project/project/roles/* \
  packaging/ansible-runner-service-project/project/roles/hc-gluster-cgroups/**/*.yml \
  packaging/ansible-runner-service-project/project/roles/ovirt-host-deploy-kdump/**/*.yml \
  packaging/ansible-runner-service-project/project/roles/ovirt-host-deploy-spice-encryption/**/*.yml

