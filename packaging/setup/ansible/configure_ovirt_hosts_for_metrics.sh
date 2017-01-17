#!/bin/sh

cd "$(dirname "$(readlink -f "$0")")"

. ../../bin/engine-prolog.sh

CONF_DIR="${ENGINE_ETC}/metrics"
CONFIG_FILE="${CONF_DIR}/config.yml"

if [ -r "${CONFIG_FILE}" ]; then
	ansible-playbook \
		playbook.yml \
		-e @"${CONFIG_FILE}" \
		-e local_fluentd_ca_cert_path="${CONF_DIR}/fluentd_ca_cert.pem" \
		-e ansible_ssh_private_key_file="${ENGINE_PKI}/keys/engine_id_rsa" \
		-l ovirt_up_metrics_hosts
else
	# Do nothing for now
	:
fi
