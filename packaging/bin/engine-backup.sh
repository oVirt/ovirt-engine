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
BACKUP_PATHS="/etc/ovirt-engine
/etc/pki/ovirt-engine
/etc/ovirt-engine-setup.conf.d
/var/lib/ovirt-engine
/etc/httpd/conf.d/ovirt-engine-root-redirect.conf
/etc/httpd/conf.d/ssl.conf
/etc/httpd/conf.d/z-ovirt-engine-proxy.conf
/etc/yum/pluginconf.d/versionlock.list
/etc/firewalld/services/ovirt-https.xml
/etc/firewalld/services/ovirt-http.xml
/etc/firewalld/services/ovirt-postgres.xml"
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
    backup                  backup system into FILE
    restore                 restore system from FILE
 SCOPE is one of the following:
    all                     complete backup/restore (default)
    db                      database only
 --file=FILE                file to use during backup or restore
 --log=FILE                 log file to use
 --change-db-credentials    activate the following options, to restore
                            the database to a different location etc.
                            If used, existing credentials are ignored.
 --db-host=host             set database host
 --db-port=port             set database port
 --db-user=user             set database user
 --db-passfile=file         set database password - read from file
 --db-password=pass         set database password
 --db-password              set database password - interactively
 --db-name=name             set database name
 --db-secured               set a secured connection
 --db-secured-validation    validate host

 ENVIRONMENT VARIABLES

 OVIRT_ENGINE_DATABASE_PASSWORD
     Database password as if provided by --db-password=pass option.

 Wiki

 See http://www.ovirt.org/Ovirt-engine-backup for more info.

 To create a new user/database:

 create role <user> with login encrypted password '<password>';
 create database <database> owner <user> template template0
 encoding 'UTF8' lc_collate 'en_US.UTF-8' lc_ctype 'en_US.UTF-8';

 Open access in the firewall/iptables/etc. to the postgresql port,
 5432/tcp by default.

 Locate pg_hba.conf within your distribution,
 common locations are:
  - /var/lib/pgsql/data/pg_hba.conf
  - /etc/postgresql-*/pg_hba.conf
  - /etc/postgresql/*/main/pg_hba.conf

 and open access there by adding the following lines:

 host    <database>      <user>          0.0.0.0/0               md5
 host    <database>      <user>          ::0/0                   md5

 Replace <user>, <password>, <database> with appropriate values.

__EOF__
	return 0
}

MODE=
SCOPE=all
CHANGE_DB_CREDENTIALS=
MY_DB_HOST=
MY_DB_PORT=5432
MY_DB_USER=
MY_DB_PASSWORD="${OVIRT_ENGINE_DATABASE_PASSWORD}"
MY_DB_DATABASE=
MY_DB_SECURED=False
MY_DB_SECURED_VALIDATION=False
MY_DB_CREDS=

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
					all|db) ;;
					*) die "invalid scope '${SCOPE}'"
				esac
			;;
			--file=*)
				FILE="${v}"
			;;
			--log=*)
				LOG="${v}"
			;;
			--change-db-credentials)
				CHANGE_DB_CREDENTIALS=1
			;;
			--db-host=*)
				MY_DB_HOST="${v}"
			;;
			--db-port=*)
				MY_DB_PORT="${v}"
			;;
			--db-user=*)
				MY_DB_USER="${v}"
			;;
			--db-passfile=*)
				DB_PASSFILE="${v}"
				[ -r "${DB_PASSFILE}" ] || \
					die "Can not read password file ${DB_PASSFILE}"
				read MY_DB_PASSWORD < "${DB_PASSFILE}"
			;;
			--db-password=*)
				MY_DB_PASSWORD="${v}"
			;;
			--db-password)
				MY_DB_PASSWORD="$(readdbpassword)" || exit 1
			;;
			--db-name=*)
				MY_DB_DATABASE="${v}"
			;;
			--db-secured)
				MY_DB_SECURED="True"
			;;
			--db-sec-validation)
				MY_DB_SECURED_VALIDATION="True"
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
	if [ -n "${CHANGE_DB_CREDENTIALS}" ]; then
		[ -n "${MY_DB_HOST}" ] || die "--db-host is missing"
		[ -n "${MY_DB_USER}" ] || die "--db-user is missing"
		[ -n "${MY_DB_PASSWORD}" ] || \
			die "--db-passfile or --db-password is missing"
		[ -n "${MY_DB_DATABASE}" ] || die "--db-name is missing"
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

	if [ "${SCOPE}" != "db" ] ; then
		log "Backing up files to ${tardir}/files"
		backupFiles "${BACKUP_PATHS}" "${tardir}/files"
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
	( cd "${tardir}" && md5sum -c "${md5file}" --status ) || logdie "Checksum verification failed"
}

backupFiles() {
	local paths="$1"
	local target="$2"
	echo "${paths}" | while read path; do
		[ -e "${path}" ] || continue
		local dirname="$(dirname ${path})"
		mkdir -p "${tardir}/files/${dirname}" || logdie "Cannot create '${tardir}/files/${dirname}"
		cp -a "${path}" "${target}/${dirname}" || logdie "Cannot copy ${path} to ${target}/${dirname}"
	done || logdie "Cannot read ${paths}"
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
	if [ -r "${ENGINE_UP_MARK}" ]; then
		ps "$(cat ${ENGINE_UP_MARK})" | grep -q 'ovirt-engine.py' &&
			logdie "Engine service is active - can not restore backup"
	fi
	if [ -n "${CHANGE_DB_CREDENTIALS}" ]; then
		setMyDBCredentials
		generatePgPass
		verifyConnection
	fi
	output "Restoring..."
	log "Opening tarball ${FILE} to ${TEMP_FOLDER}"
	tar -C "${TEMP_FOLDER}" -pSsxf "${FILE}" || logdie "cannot open ${TEMP_FOLDER}"
	log "Verifying md5"
	verifymd5 "${TEMP_FOLDER}" "md5sum"
	log "Verifying version"
	verifyVersion

	if [ "${SCOPE}" != "db" ] ; then
		log "Restoring files"
		restoreFiles "${BACKUP_PATHS}"
	fi

	if [ -z "${CHANGE_DB_CREDENTIALS}" ]; then
		log "Reloading configuration"
		load_config
	fi

	log "Generating pgpass"
	generatePgPass # Must run after configuration reload
	log "Verifying connection"
	verifyConnection
	log "Restoring database backup at ${TEMP_FOLDER}/db/${DB_BACKUP_FILE_NAME}"
	restoreDB "${TEMP_FOLDER}/db/${DB_BACKUP_FILE_NAME}"
	if [ -n "${CHANGE_DB_CREDENTIALS}" ]; then
		changeDBConf
	fi
	output "Note: you might need to manually fix:"
	output "- iptables/firewalld configuration"
	output "- autostart of ovirt-engine service"
	output "You can now start the engine service and then restart httpd"
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
		|| logdie "Can't connect to the database. Please see '${0} --help'."

	PGPASSFILE="${MYPGPASS}" pg_dump \
		-U "${ENGINE_DB_USER}" \
		-h "${ENGINE_DB_HOST}" \
		-p "${ENGINE_DB_PORT}" \
		"${ENGINE_DB_DATABASE}" | \
		grep -vi '^create extension' | \
		grep -iq '^create' && \
		logdie "Database is not empty"
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
	local paths="$1"
	echo "${paths}" | while read path; do
		local dirname="$(dirname ${path})"
		local backup="${TEMP_FOLDER}/files/${path}"
		[ -e "${backup}" ] || continue
		cp -a "${backup}" "${dirname}" || logdie "Cannot copy '${backup}' to '${dirname}'"
		if selinuxenabled; then
			restorecon -R "${path}" || logdie "Failed setting selinux context for ${path}"
		fi
	done || logdie "Cannot read ${paths}"
}

setMyDBCredentials() {
	local options

	[ "${MY_DB_SECURED}" = "True" ] && \
		options="${options}&ssl=true"
	[ "${MY_DB_SECURED_VALIDATION}" != "True" ] && \
		options="${options}&sslfactory=org.postgresql.ssl.NonValidatingFactory"

	[ -n "${options}" ] && options="${options#&}"

	local encpass="$(sed 's;\(["\$]\);\\\1;g' << __EOF__
${MY_DB_PASSWORD}
__EOF__
)"

	MY_DB_CREDS="$(cat << __EOF__
ENGINE_DB_HOST="${MY_DB_HOST}"
ENGINE_DB_PORT="${MY_DB_PORT}"
ENGINE_DB_USER="${MY_DB_USER}"
ENGINE_DB_PASSWORD="${encpass}"
ENGINE_DB_DATABASE="${MY_DB_DATABASE}"
ENGINE_DB_SECURED="${MY_DB_SECURED}"
ENGINE_DB_SECURED_VALIDATION="${MY_DB_SECURED_VALIDATION}"
ENGINE_DB_DRIVER="org.postgresql.Driver"
ENGINE_DB_URL="jdbc:postgresql://\${ENGINE_DB_HOST}:\${ENGINE_DB_PORT}/\${ENGINE_DB_DATABASE}?${options}"
__EOF__
)"
	eval "${MY_DB_CREDS}"
}

changeDBConf() {
	local conf="${ENGINE_ETC}/engine.conf.d/10-setup-database.conf"
	[ -f "${conf}" ] || logdie "Can not find ${conf}"

	local options
	local backup="${conf}.$(date +"%Y%m%d%H%M%S")"
	log "Backing up ${conf} to ${backup}"
	cp -a "${conf}" "${backup}" || die "Failed to backup ${conf}"
	output "Rewriting ${conf}"
	echo "${MY_DB_CREDS}" > "${conf}"
}

generatePgPass() {
	local password="${ENGINE_DB_PASSWORD}"
	MYPGPASS="${TEMP_FOLDER}/.pgpass"

	touch "${MYPGPASS}" || logdie "Can't touch ${MYPGPASS}"
	chmod 0600 "${MYPGPASS}" || logdie "Can't chmod ${MYPGPASS}"

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

	cat > "${MYPGPASS}" << __EOF__
${ENGINE_DB_HOST}:${ENGINE_DB_PORT}:${ENGINE_DB_DATABASE}:${ENGINE_DB_USER}:${password}
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

readdbpassword() {
	(
		cleanup() {
			[ -n "${STTY_ORIG}" ] && stty "${STTY_ORIG}"
		}

		STTY_ORIG=
		trap cleanup 0
		[ -t 0 ] || die "Standard input is not a terminal"
		STTY_ORIG="$(stty -g)"
		stty -echo || die "Failed to disable terminal input echo"
		echo -n "Enter database password: " >&2
		read dbpass
		echo >&2
		cat << __EOF__
${dbpass}
__EOF__
	)
}

## Main

# Do this in function so we do not lose $@
parseArgs "$@"
verifyArgs

TEMP_FOLDER="$(mktemp -d /tmp/engine-backup.XXXXXXXXXX)" || logdie "Can't create temporary directory"
generatePgPass
do${MODE}
output "Done."
