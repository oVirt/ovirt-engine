#!/bin/sh
#
# ovirt-engine-backup - oVirt engine backup and restore utility
# Copyright (C) 2013 Red Hat, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#	 http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

# Load the prolog:
. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

# Globals
BACKUP_FOLDERS="/etc/ovirt-engine
/etc/pki/ovirt-engine
/etc/ovirt-engine-setup.conf.d
/var/lib/ovirt-engine"
MYPGPASS=""
TEMP_FOLDER=""
FILE=""
DB_BACKUP_FILE_NAME="engine_backup.db"
FILES="files"

cleanup() {
	[ -n "${TEMP_FOLDER}" ] && rm -rf "${TEMP_FOLDER}"
}

trap cleanup 0

usage() {
	cat << __EOF__
engine-backup: backup and restore ovirt-engine environment
USAGE:
    $0 [--mode=MODE] [--scope=SCOPE] [--file=FILE] [--log=FILE]
 MODE is one of the following:
    backup        backup system into FILE
    restore       restore system from FILE
 SCOPE is one of the following:
    all           complete backup/restore
    db            database only
 --file=FILE      file to use during backup or restore
 --log=FILE       log file to use
__EOF__
	return 0
}

MODE=
SCOPE=

parseArgs() {
	while [ -n "$1" ]; do
		local x="$1"
		local v="${x#*=}"
		shift
		case "${x}" in
			--mode=*)
				MODE="${v}"
				case "${MODE}" in
					backup|restore);;
					*) die "invalid mode" ;;
				esac
			;;
			--scope=*)
				SCOPE="${v}"
				case "${SCOPE}" in
					all|dbonly) ;;
					*) die "invalid scope '${SCOPE}'"
				esac
			;;
			--file=*)
				FILE="${v}"
			;;
			--log=*)
				LOG="${v}"
			;;
			--help)
				usage
				exit 0
			;;
			*)
				usage
				exit 1
			;;
		esac
	done
}

verifyArgs() {
	[ -n "${MODE}" ] || die "--mode=<backup|restore> is missing"
	[ -n "${FILE}" ] || die "--file is missing"
	[ -n "${LOG}" ] || die "--log is missing"
	if [ "${MODE}" == "restore" ] ; then
		[ -e "${FILE}" ] || die "${FILE} does not exist"
	fi
}

dobackup() {
	output "Backing up..."
	log "Generating pgpass"
	generatePgPass

	# Create temporary folder
	local tardir="${TEMP_FOLDER}/tar"
	log "Creating temp folder ${tardir}"
	mkdir "${tardir}" || logdie "Cannot create '${tardir}'"
	mkdir "${tardir}/${FILES}" || logdie "Cannot create '${tardir}/files'"
	mkdir "${tardir}/db" || logdie "Cannot create '${tardir}/db'"

	if [ "${SCOPE}" != "dbonly" ] ; then
		log "Backing up files to ${tardir}/files"
		backupFiles "${BACKUP_FOLDERS}" "${tardir}/files"
	fi

	log "Backing up database to ${tardir}/db/${DB_BACKUP_FILE_NAME}"
	backupDB "${tardir}/db/${DB_BACKUP_FILE_NAME}"
	echo "${PACKAGE_VERSION}" > "${tardir}/version" || logdie "Can't create ${tardir}/version"
	log "Creating md5sum at ${tardir}/md5sum"
	createmd5 "${tardir}" "${tardir}/md5sum"
	log "Creating tarball ${FILE}"
	createtar "${tardir}" "${FILE}"
}

createtar() {
	local dir="$1"
	local file="$2"
	tar -C "${dir}" -cpSsjf "${file}" . || logdie "Cannot create '${file}'"
}

createmd5() {
	local tardir="$1"
	local md5file="$2"
	find "${tardir}" -type f -printf "%P\n" | while read file; do
		( cd "${tardir}" && md5sum "${file}" ) >> "${md5file}" || logdie "Cannot create checksum for '${file}'"
	done || logdie "Find execution failed"
}

verifymd5() {
	local tardir="$1"
	local md5file="$2"
	( cd "${tardir}" && md5sum -c "${md5file}" --status --strict ) || logdie "Checksum verification failed"
}

backupFiles() {
	local folders="$1"
	local target="$2"
	echo "${folders}" | while read folder; do
		local dirname="$(dirname ${folder})"
		mkdir -p "${tardir}/files/${dirname}" || logdie "Cannot create '${tardir}/files/${dirname}"
		cp -a "${folder}" "${target}/${dirname}" || logdie "Cannot copy ${folder} to ${target}/${dirname}"
	done || logdie "Cannot read ${folders}"
}

backupDB() {
	local file="$1"
	PGPASSFILE="${MYPGPASS}" pg_dump \
		-E "UTF8" \
		--disable-dollar-quoting \
		--disable-triggers \
		--format=p \
		-w \
		-U "${ENGINE_DB_USER}" \
		-h "${ENGINE_DB_HOST}" \
		-p "${ENGINE_DB_PORT}" \
		-f "${file}" \
		"${ENGINE_DB_DATABASE}" \
		|| logdie "Database backup failed"
}

dorestore() {
	output "Restoring..."
	log "Opening tarball ${FILE} to ${TEMP_FOLDER}"
	tar -C "${TEMP_FOLDER}" -pSsxf "${FILE}" || logdie "cannot open ${TEMP_FOLDER}"
	log "Verifying md5"
	verifymd5 "${TEMP_FOLDER}" "md5sum"
	log "Verifying version"
	verifyVersion

	if [ "${SCOPE}" != "dbonly" ] ; then
		log "Restoring files"
		restoreFiles "${BACKUP_FOLDERS}"
	fi

	log "Reloading configuration"
	load_config
	log "Generating pgpass"
	generatePgPass # Must run after configuration reload
	log "Verifying connection"
	verifyConnection
	log "Restoring database backup at ${TEMP_FOLDER}/db/${DB_BACKUP_FILE_NAME}"
	restoreDB "${TEMP_FOLDER}/db/${DB_BACKUP_FILE_NAME}"
}

verifyConnection() {
	PGPASSFILE="${MYPGPASS}" psql \
		-w \
		-U "${ENGINE_DB_USER}" \
		-h "${ENGINE_DB_HOST}" \
		-p "${ENGINE_DB_PORT}" \
		-d "${ENGINE_DB_DATABASE}" \
		-c "select 1" \
		>> "${LOG}" 2>&1 \
		|| logdie "Can't connect to the database"
}

verifyVersion() {
	local installed_version="$(echo ${PACKAGE_VERSION} | cut -d . -f 1-2)"
	local backup_version="$(cat ${TEMP_FOLDER}/version | cut -d . -f 1-2)"
	[ "${installed_version}" == "${backup_version}" ] \
		|| logdie "Backup version '${backup_version}' doesn't match installed version"
}

restoreDB() {
	local backupfile="$1"
	PGPASSFILE="${MYPGPASS}" psql \
		-w \
		-U "${ENGINE_DB_USER}" \
		-h "${ENGINE_DB_HOST}" \
		-p "${ENGINE_DB_PORT}" \
		-d "${ENGINE_DB_DATABASE}" \
		-f "${backupfile}" \
		>> "${LOG}"  2>&1 \
		|| logdie "Database restore failed"
}

restoreFiles() {
	local folders="$1"
	echo "${folders}" | while read folder; do
		local dirname="$(dirname ${folder})"
		local backup="${TEMP_FOLDER}/files/${folder}"
		cp -a "${backup}" "${dirname}" || logdie "Cannot copy '${backup}' to '${dirname}'"
	done || logdie "Cannot read ${folders}"
}

generatePgPass() {
	MYPGPASS="${TEMP_FOLDER}/.pgpass"
	touch "${MYPGPASS}" || logdie "Can't touch ${MYPGPASS}"
	chmod 0600 "${MYPGPASS}" || logdie "Can't chmod ${MYPGPASS}"
	cat > "${MYPGPASS}" << __EOF__
${ENGINE_DB_HOST}:${ENGINE_DB_PORT}:${ENGINE_DB_DATABASE}:${ENGINE_DB_USER}:${ENGINE_DB_PASSWORD}
__EOF__
}

log() {
	local m="$1"
	local date="$(date '+%Y-%m-%d %H:%M:%S')"
	local pid="$$"
	echo "${date} ${pid}: ${m}" >> "${LOG}"
}

logdie() {
	local m="$1"
	log "FATAL: ${m}"
	die "${m}"
}

output() {
	local m="$1"
	log "${m}"
	echo "${m}"
}

## Main

# Do this in function so we do not lose $@
parseArgs "$@"
verifyArgs

TEMP_FOLDER="$(mktemp -d /tmp/engine-backup.XXXXXXXXXX)" || logdie "Can't create temporary directory"
generatePgPass
do${MODE}
output "Done."
