#!/bin/sh

. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh

[[ -z $ENGINE_DB_HOST ]]     || \
[[ -z $ENGINE_DB_PORT ]]     || \
[[ -z $ENGINE_DB_USER ]]     || \
[[ -z $ENGINE_DB_DATABASE ]] && die "Can't parse the connection details"

MYTEMP="$(mktemp -d)"

generatePgPass() {
    local password="$(echo "${ENGINE_DB_PASSWORD}" | sed -e 's/\\/\\\\/g' -e 's/:/\\:/g')"
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
