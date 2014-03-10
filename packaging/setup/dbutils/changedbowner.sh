#!/bin/sh
################################################################
# This script change the ownership of objects in the
# database
################################################################

. "$(dirname "$0")/dbfunc-base.sh"

cleanup() {
	dbfunc_cleanup
}
trap cleanup 0
dbfunc_init

usage() {
    cat << __EOF__
Usage: $0 [options]

    -h            - This help text.
    -s HOST       - The database servername for the database  (def. ${DBFUNC_DB_HOST})
    -p PORT       - The database port for the database        (def. ${DBFUNC_DB_PORT})
    -d DATABASE   - The database name                         (def. ${DBFUNC_DB_DATABASE})
    -f FROM_USER  - The current owner for the database
    -t TO_USER    - The new owner for the database

__EOF__
}

while getopts hs:p:d:f:t: option; do
	case $option in
		\?) usage; exit 1;;
		h) usage; exit 0;;
		s) DBFUNC_DB_HOST="${OPTARG}";;
		p) DBFUNC_DB_PORT="${OPTARG}";;
		d) DBFUNC_DB_DATABASE="${OPTARG}";;
		f) DBFUNC_DB_USER="${OPTARG}";;
		t) TO_USER="${OPTARG}";;
	esac
done

[ -n "${DBFUNC_DB_USER}" ] || die "Please specify from user"
[ -n "${TO_USER}" ] || die "Please specify to user"
[ -n "${DBFUNC_DB_DATABASE}" ] || die "Please specify database"

tempfile="$(mktemp)"
cleanup() {
    rm -f "${tempfile}"
}
trap cleanup 0

# Change all schema objects ownership
echo "Changing database ${DBFUNC_DB_DATABASE} objects ownership"
( dbfunc_pg_dump_die && echo ok >> "${tempfile}" ) | \
	grep -i 'owner to' | sed "s/OWNER TO ${DBFUNC_DB_USER};/OWNER TO ${TO_USER};/i" | \
	( dbfunc_psql_die && echo ok >> "${tempfile}" )

[ "$(wc -l < "${tempfile}")" -eq 2 ] || die "Failed to change DB ${DBFUNC_DB_DATABASE} objects ownership."

echo "Changing database ${DBFUNC_DB_DATABASE} ownership"
dbfunc_psql_die --command="ALTER DATABASE ${DBFUNC_DB_DATABASE} OWNER TO ${TO_USER};"

echo "Changing database ${DBFUNC_DB_DATABASE} ownership from ${DBFUNC_DB_USER} to ${TO_USER} completed successfully."
exit 0
