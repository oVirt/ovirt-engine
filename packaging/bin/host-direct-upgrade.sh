#!/bin/bash
#
# engine-host-update - oVirt engine host update utility
# Copyright (C) 2017 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh
MYTEMP="$(mktemp -d)"

OPERATION=
LOG_FILE=
URL=
OVERRIDE=0
ENGINE_LOG_DIR="/var/log/ovirt-engine/"
IMPORT_DIR="/var/lib/ovirt-engine/host-configuration"
EXPORT_COMPLETED="${IMPORT_DIR}/export.completed"
PRIVATE_KEY="/etc/pki/ovirt-engine/keys/engine_id_rsa"
PUBLIC_KEY="${MYTEMP}/engine_id_rsa.pub"
AGENT_LOG="/var/log/ovirt-hosted-engine-ha/agent.log"
SSH="ssh -q -i ${PRIVATE_KEY} -o ConnectTimeout=5 -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null"
SCP="scp -q -i ${PRIVATE_KEY} -o StrictHostKeyChecking=no"

# Configuration files:
ANSWERS="/etc/ovirt-hosted-engine/answers.conf"
HOSTED_ENGINE="/etc/ovirt-hosted-engine/hosted-engine.conf"
VM="/etc/ovirt-hosted-engine/vm.conf"
BROKER="/etc/ovirt-hosted-engine-ha/broker.conf"

CONF_FILES=("${ANSWERS}" "${HOSTED_ENGINE}" "${VM}" "${BROKER}")

function run_cmd(){
	write_to_log "Running command $*"
	"$@" >> "${LOG_FILE}" 2>&1
	rc=$?
	write_to_log "Command finished, rc=${rc}"
	return "${rc}"
}

function output(){
	local m="$1"
	echo "${m}"
	write_to_log "${m}"
}
function write_to_log(){
	local m="$1"
	[[ -z "${LOG_FILE}" ]] || echo "$(date '+%d-%m-%Y %H:%M:%S,%3N%z') ${m}" >> $LOG_FILE
}

function die() {
	local m="$1"
	output "FATAL: ${m}"
	exit 1
}

function usage() {
	cat << __EOF__
usage: $0 [OPTION] [-h host_address]

This tool can be used to upgrade a RHV 3.5 EL6 host to RHV 3.6/EL7.
It should only be used for the first host, the others can be reinstalled as usual.
More details at $URL.

Instructions:

1. Pick a RHV 3.5 EL6, that have the following configuration files and move it to maintenance:
	- ${ANSWERS}
	- ${HOSTED_ENGINE}
	- ${VM}
	- ${BROKER}
2. Run the tool with '--import-configuration-files -h [host address]'
3. Reinstall the host with EL7/RHV3.6
4. Run the tool with '--export-and-configure -h [host address]'
5. Add the reinstalled host to the engine from the web admin UI.

Exactly one of the following options must be supplied:

Options
--import-configuration-files
		Import configuration files from RHEV-H 3.5 to the engine for later export to 3.6 host.
		The import operation will succeed only if ALL the configuration files are present at the host.
		During this operation ovirt-engine might be restarted to allow clusters to have mixed hosts with different RHEL versions.
		After the operation will complete the files will be stored on ${IMPORT_DIR}

--export-and-configure
		Configure a 3.6 host with the configuration files that were imported.
		Can be done only after --import-configuration-files option.

-h 		Host's address.

__EOF__
}

generatePgPass() {
	local password="${ENGINE_DB_PASSWORD}"

	#
	# we need client side psql library
	# version as at least in rhel for 8.4
	# the password within pgpassfile is
	# not escaped.
	# the simplest way is to checkout psql
	# utility version.
	#
	if ! psql -V | grep -q ' 8\.'; then
		password="$(echo "${password}" | sed -e 's/\\/\\\\/g' -e 's/:/\\:/g')"
	fi

	export PGPASSFILE="${MYTEMP}/.pgpass"
	touch "${PGPASSFILE}" || die "Can't create ${PGPASSFILE}"
	chmod 0600 "${PGPASSFILE}" || die "Can't chmod ${PGPASSFILE}"

	cat > "${PGPASSFILE}" << __EOF__
${ENGINE_DB_HOST}:${ENGINE_DB_PORT}:${ENGINE_DB_DATABASE}:${ENGINE_DB_USER}:${password}
__EOF__
}

cleanup() {
	[ -n "${MYTEMP}" ] && rm -fr "${MYTEMP}" ]
}
trap cleanup 0

function confirm(){
	local q=$1
	local ans=1
	local res=

	while [[ $ans -eq 1 ]]; do
		read -r -p "${q}" res
		case "${res}" in
			[Yy][Ee][Ss]|[Yy])
				return 0
				;;
			[Nn][Oo]|[Nn])
				die "Aborted by the user"
				;;
			*)
				echo "ERROR: Invalid answer"
				;;
		esac
	done

}

function import_directory_check() {
	# check configuration import directory
	# On import:
	# 1. Check if the import folder exists at the engine - if not, it is being created
	# 2. Check for the contents of the folder - if there are files ask the user for override
	#
	# On export:
	# 1. Check if the import folder exists and not empty, if not print an error

	local response=

	[[ ! -a "${EXPORT_COMPLETED}" ]] || die "Host upgrade from 3.5 host to 3.6 has already occured."

	# if import dir does not exists
	if [[ ! -d "${IMPORT_DIR}" ]]; then
		if [[ "${OPERATION}" == "import" ]]; then
			run_cmd mkdir ${IMPORT_DIR}
		else
			die "Import folder does not exist, please import the configuration files from 3.5 host using the '--import-configutarion-files' option"
		fi

	# if import dir exists
	else
		if [[ "${OPERATION}" == "import" ]]; then
			# if the import directory contains files
			if check_imported_files exist; then
				output  "There are files in the import folder (${IMPORT_DIR})"
				confirm 'Do you want to override them? [Y/N]: '
				OVERRIDE=$?
			fi
		else
			# If user tries to export and the import dir is empty
			check_imported_files missing && die "Some files are missing, please import the configuration files from 3.5 host using the '--import-configuration-files' option"
		fi
	fi

	if [[ "${OPERATION}" == "import" ]]; then
		output "Import target (${IMPORT_DIR}) is ready"
	else
		output "All configuration files are present at (${IMPORT_DIR})."
		run_cmd ls -l ${IMPORT_DIR}
	fi
}

function connectivity_check() {

	local connection=

	if [[ -z "${host_adrs}" ]]; then
		read -r -p "Please enter the ${host_release} host's address: " host_adrs
	fi

	# works in 3.6 but not later
	curl -s -o "${PUBLIC_KEY}" --insecure "https://localhost/ovirt-engine/services/pki-resource?resource=engine-certificate&format=OPENSSH-PUBKEY"
	chmod 600 "${PUBLIC_KEY}"

	# need only for export, no harm in doing always
	output "Configuring the host to accept ssh connections from the engine. Please provide root password if prompted."
	ssh-copy-id -i "${PUBLIC_KEY}" "-i ${PRIVATE_KEY} -o ConnectTimeout=5 -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null root@${host_adrs}" &> /dev/null

	[[ $? -eq 0 ]] || die "Could not connect to ${host_adrs} via SSH. Aborting..."
	output "Connectivity check: Host ${host_adrs} is available via SSH."
}

function check_release() {
	# check vdsm version: 4.16 should be on 3.5 | 4.17 should be on 3.6

	local vdsm_version=$($SSH root@$host_adrs rpm -qa | grep vdsm-4 2>&1)
	[[ ! -z "${vdsm_version}" ]] || die "Could not find VDSM on the host. Aborting..."
	if [[ "${OPERATION}" == "import" ]]; then
		[[ "${vdsm_version}" =~ "vdsm-4.16" ]] || die "You are trying to import the configuration files from a newer host (3.6 or higher) - vdsm version: ${vdsm_version}. Aborting..."
	else
		[[ ! "${vdsm_version}" =~ "vdsm-4.16" ]] || die "You are trying to configure a 3.5 host instead of 3.6 host - vdsm version: ${vdsm_version}. Aborting..."
	fi
	write_to_log "VDSM version: ${vdsm_version}"
}

function check_for_files_at_host() {
	# Check if all files that need to be imported are present at the host

	local missing=
	for file in ${CONF_FILES[@]}; do
		${SSH} root@${host_adrs} test -f ${file} &> /dev/null
		if [[ $? -ne 0 ]]; then
			output "ERROR: Failed to get ${file} from ${host_adrs}"
			missing="yes"
		fi
	done

	if [[ "${missing}" ]]; then
		[[ "${OPERATION}" != "export" ]] || die "Some files were not trasferred to ${host_adrs}. Aborting..."

		die "ERROR: Some files are missing on ${host_adrs}. Aborting...."

	fi

	if [[ "${OPERATION}" == "import" ]]; then
		output "All configuration files are present at the host and ready to be imported to the engine"
	else
		output "All configuration files were transferred successfully to the host"
	fi
}

function update_engine_db() {
	local TOTAL_TIME_TO_WAIT=300 # total time to wait for the engine to restart
	local SLEEP_INTERVAL=15
	local TIME_BEFORE_CHECK_SERVICE_STATUS=3

	output "Starting engine DB update"
	engine-config -s CheckMixedRhelVersions=false --cver=3.5

	generatePgPass

	run_cmd psql -h "${ENGINE_DB_HOST}" -p "${ENGINE_DB_PORT}" -U "${ENGINE_DB_USER}" -c "
		update vm_static set migration_support=0 where vm_name='HostedEngine'"

	output "Restarting ovirt-engine service."
	service ovirt-engine restart

	output "Checking ovirt-engine service status"
	sleep $TIME_BEFORE_CHECK_SERVICE_STATUS

	# Waiting for the engine to start
	local time_counter=0
	while ! run_cmd service ovirt-engine status && [[ $time_to_wait -lt $TOTAL_TIME_TO_WAIT ]]; do
		sleep $SLEEP_INTERVAL
		let time_counter+=$SLEEP_INTERVAL
	done

	if [[ $time_counter -ge $TOTAL_TIME_TO_WAIT ]]; then
		die "ovirt-engine failed to started. Check the logs for more info"
	else
		output "Update engine's DB completed"
	fi
}

function configure_new_host(){
	output "Configuring 3.6 host. This might take several minutes or more..."

	let MAX_WAIT_TIME=15*60
	local WAIT_BETWEEN_CHECKS=20
	local DIVIDER=60
	local VM_STATUS=
	local upgraded=
	local time_count=0
	local SUCCESS_MSG='\(upgrade_35_36\)\ Successfully\ upgraded'

	output "Running 'vdsm-tool configure --force'"
	run_cmd ${SSH} root@$host_adrs vdsm-tool configure --force
	output "Starting ovirt-ha-agent service"
	run_cmd ${SSH} root@$host_adrs service ovirt-ha-agent start

	output "Wait for the 3.6 host to be operational"
	while [[ -z "${upgraded}" ]] && [[ $time_count -lt $MAX_WAIT_TIME ]]; do

		let time_count+=$WAIT_BETWEEN_CHECKS
		if (( $time_count % $DIVIDER == 0 )); then
			output "Still configuring the host. Please wait."
		fi

		sleep $WAIT_BETWEEN_CHECKS
		VM_STATUS="$($SSH root@$host_adrs hosted-engine --vm-status 2>&1)"
		write_to_log "${VM_STATUS}"
		upgraded=$($SSH root@$host_adrs grep "${SUCCESS_MSG}" $AGENT_LOG 2>/dev/null)
	done

	if [[ $time_count -ge $MAX_WAIT_TIME ]]; then
		die "Host is not reliable please check the logs or reinstall. Aborting..."
	fi

	output "Configure 3.6 host completed successfully"
}

function check_imported_files(){
	# Check if the configuration files are present or not at the import directory at the engine
	# If arg equals 'missing' the method will return 'yes' if any of the configuration files are missing
        # If arg equals 'exist' the method will return 'yes' is any of the configuration files exist

	local arg="${1}"
	local missing=1
	local exists=1

	output "Checking if all the configuration files are present at ${IMPORT_DIR}"
	for file in ${CONF_FILES[@]}; do
			local imported_file="${IMPORT_DIR}/${file##*/}"
			if [[ "${arg}" == "missing" ]]; then
				[[ -a "${imported_file}" ]] || missing=0
			else
				[[ ! -a "${imported_file}" ]] || exists=0
			fi
	done

	if [[ "${arg}" == "missing" ]]; then
		[[ $missing -eq 1 ]] || output "Some configuration files are missing"
		return $missing
	else
		return $exists
	fi
}


function import_configuration_files() {
	# Importing 3.5 host configuration files to the engine
	local engine_need_restart=
	host_release="3.5"

	# Check if the engine support mix rhel versions on cluster if not - need to enable it
	# Once enabled, ovirt-engine nust be restarted
	output "Checking if engine is configured to allow mixed RHEL versions in same cluster"
	local CheckMixedRhelVersions3_5_val=$(engine-config -g CheckMixedRhelVersions --cver=3.5)
	if [[ "${CheckMixedRhelVersions3_5_val}" != "false" ]]; then
		output "NOTICE: During this procedure the engine will be restarted"
		confirm 'Do you wish to proceed? [Y/N]: '
		engine_need_restart=$?
	fi

	# pre import checks
	output "Running pre import checks"
	import_directory_check
	connectivity_check
	check_release
	check_for_files_at_host

	# if configuration files are present at the import directory they are erased
	if [[ $OVERRIDE ]]; then
		output "Removing all configuration files from ${IMPORT_DIR}"
		for file in ${CONF_FILES[@]}; do
			run_cmd rm -f "${IMPORT_DIR}/${file##*/}"
		done
	fi

	# Transfer configuration files from the 3.5 host to the engine import directory
	output "Importing configuration"
	for file in ${CONF_FILES[@]}; do
		${SCP} root@${host_adrs}:${file} ${IMPORT_DIR} &> /dev/null
	done

	# Validate that all configuration files were imported successfully

	check_imported_files missing && die "Some configuration files are missing. Please try again"
	output "All the configuration files were imported successfully"

	[ -n "${engine_need_restart}" ] && update_engine_db

	output "Import Completed!"
}


function export_and_configure(){

	host_release="3.6"

	# pre export checks
	output "Running pre export checks"
	import_directory_check
	connectivity_check
	check_release

	# Restore configuration files
	output "Export configuration files from the engine to the 3.6 host has started"
	for file in ${CONF_FILES[@]}; do
		local imported_file="${IMPORT_DIR}/${file##*/}"
		${SCP} ${imported_file} root@${host_adrs}:${file} &> /dev/null
	done

	check_for_files_at_host

	${SSH} root@${host_adrs} chmod +r "${ANSWERS}" "${HOSTED_ENGINE}" "${VM}" "${BROKER}"

	output "Configuring 3.6 host"
	configure_new_host

	touch "${EXPORT_COMPLETED}"

	output "Configuring 3.6 host Completed!"
	output "Please enter the engine's web UI, remove the old 3.5 host and add the 3.6 host that has been reinstalled."
}

output "Welcome to 3.5 to 3.6 Host Upgrade Tool"

[[ "$(whoami)" -eq "root" ]] || die "The upgrade tool must be executed as user root"
if [ -z "$*" ]; then
	usage
	die "ERROR: No option was provided."
fi

while [[ -n "$1" ]]; do
	x="$1"
	shift
	case "${x}" in
		--import-configuration-files)
			[[ -z "${OPERATION}" ]] || die "Exactly one option must be supplied. pass '--help' for more info"
			OPERATION="import"
			;;
		--export-and-configure)
			[[ -z "${OPERATION}" ]] || die "Exactly one option must be supplied. pass '--help' for more info"
			OPERATION="export"
			;;
		-h)
			[[ ! -z "${1}" ]] || die "No address was supplied"
			host_adrs="${1}"
			shift
			;;
		--help)
			usage
			exit 0
			;;
		*)
			usage
			die "Invalid option '${x}'"
			;;
	esac
done

LOG_FILE=$(mktemp -p $ENGINE_LOG_DIR host_upgrade_"${OPERATION}"_XXXXX.log)
echo "log file has been created at ${LOG_FILE}"

[[ ! -z "${OPERATION}" ]] || die "No Option was chosen. Aborting..."

case "${OPERATION}" in
	"import")
		import_configuration_files
		;;
	"export")
		export_and_configure
		;;
	*)
		die "Invalid option '${x}'"
esac

