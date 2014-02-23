#!/bin/sh

DBFUNC_COMMON_DBSCRIPTS_DIR="$(dirname "$0")"
. "${DBFUNC_COMMON_DBSCRIPTS_DIR}/dbfunc-custom.sh"

cleanup() {
	dbfunc_cleanup
}
trap cleanup 0
dbfunc_init

usage() {
	cat << __EOF__
Usage: $0 [options] [ENTITIES]

    -h            - This help text.
    -v            - Turn on verbosity                         (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output          (def. ${DBFUNC_LOGFILE})
    -s HOST       - The database servername for the database  (def. ${DBFUNC_DB_HOST})
    -p PORT       - The database port for the database        (def. ${DBFUNC_DB_PORT})
    -u USER       - The username for the database             (def. ${DBFUNC_DB_USER})
    -d DATABASE   - The database name                         (def. ${DBFUNC_DB_DATABASE})
    -t TYPE       - The object type {vm | template | disk | snapshot}
    -r            - Recursive, unlocks all disks under the selected vm/template.
    -q            - Query db and display a list of the locked entites.
    ENTITIES      - The list of object names in case of vm/template, UUIDs in case of a disk

__EOF__
}

TYPE=
RECURSIVE=
QUERY=

while getopts hvl:s:p:u:d:t:rq option; do
	case $option in
		\?) usage; exit 1;;
		h) usage; exit 0;;
		v) DBFUNC_VERBOSE=1;;
		l) DBFUNC_LOGFILE="${OPTARG}";;
		s) DBFUNC_DB_HOST="${OPTARG}";;
		p) DBFUNC_DB_PORT="${OPTARG}";;
		u) DBFUNC_DB_USER="${OPTARG}";;
		d) DBFUNC_DB_DATABASE="${OPTARG}";;
		t) TYPE="${OPTARG}";;
		r) RECURSIVE=1;;
		q) QUERY=1;;
	esac
done

shift $(( $OPTIND - 1 ))
IDS="$@"

[ -n "${TYPE}" ] || die "Please specify type"
[ -z "${IDS}" -a -z "${QUERY}" ] && die "Please specify ids or query"
[ -n "${IDS}" -a -n "${QUERY}" ] && die "Please specify one ids or query"

if [ -n "${IDS}" ]; then
	echo "Caution, this operation may lead to data corruption and should be used with care. Please contact support prior to running this command"
	echo "Are you sure you want to proceed? [y/n]"
	read answer
	[ "${answer}" = "y" ] || die "Please contact support for further assistance."

	for ID in ${IDS} ; do
		dbfunc_common_entity_unlock "${TYPE}" "${ID}" "$(whoami)" ${RECURSIVE}
	done
elif [ -n "${QUERY}" ]; then
	dbfunc_common_entity_query "${TYPE}"
fi
