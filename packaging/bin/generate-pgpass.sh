#!/bin/sh

# Attention: This script is using trap for cleaning up files in tmp.
# If you have own trap, don't forget to call pgPassCleanup.

[[ -z $ENGINE_DB_HOST ]]     || \
[[ -z $ENGINE_DB_PORT ]]     || \
[[ -z $ENGINE_DB_USER ]]     || \
[[ -z $ENGINE_DB_DATABASE ]] && die "Can't parse the connection details"

MYTEMP="$(mktemp -d)"

generatePgPass() {
    local password="$(echo "${ENGINE_DB_PASSWORD}" | sed -e 's/\\/\\\\/g' -e 's/:/\\:/g')"
	export MYPGPASS="${MYTEMP}/.pgpass"
	touch "${MYPGPASS}" || die "Can't create ${MYPGPASS}"
	chmod 0600 "${MYPGPASS}" || die "Can't chmod ${MYPGPASS}"

	cat > "${MYPGPASS}" << __EOF__
${ENGINE_DB_HOST}:${ENGINE_DB_PORT}:${ENGINE_DB_DATABASE}:${ENGINE_DB_USER}:${password}
__EOF__
}

pgPassCleanup() {
	[ -n "${MYTEMP}" ] && rm -fr "${MYTEMP}" ]
}
trap pgPassCleanup 0
