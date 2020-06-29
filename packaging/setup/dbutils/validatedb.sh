#!/bin/sh

. "$(dirname "$(dirname "$(dirname "$(readlink -f "$0")")")")"/bin/engine-prolog.sh
. "$(dirname "$(dirname "$(dirname "$(readlink -f "$0")")")")"/bin/generate-pgpass.sh

dbutils="$(dirname "${0}")"
. "${dbutils}/dbfunc-base.sh"

cleanup() {
	dbfunc_cleanup
        pgPassCleanup
        unset DBFUNC_DB_PGPASSFILE
}
trap cleanup 0

SERVERNAME=${ENGINE_DB_HOST:-localhost}
PORT=${ENGINE_DB_PORT:-5432}
USERNAME=${ENGINE_DB_USER:-engine}
DATABASE=${ENGINE_DB_DATABASE:-engine}

if [ -z "${PGPASSWORD}" ]; then
	generatePgPass
	export DBFUNC_DB_PGPASSFILE="${MYPGPASS}"
fi

usage() {
    cat << __EOF__
Usage: $0
    --log=file
        write log to this file.
    --user=username
        user name to use to connect to the DB
    --host=hostname
        server to use to connect to the DB
    --port=port
        server port to use to connect to the DB
    --fix
        run validation script in "fix" mode
    --database=db
        database to connect to

__EOF__
}

while [ -n "$1" ]; do
	x="$1"
	v="${x#*=}"
	shift
	case "${x}" in
		--log=*)
			LOGFILE="${v}"
		;;
		--user=*)
			USERNAME="${v}"
		;;
		--host=*)
			SERVERNAME="${v}"
		;;
		--port=*)
			PORT="${v}"
		;;
		--database=*)
			DATABASE="${v}"
		;;
		--fix*)
			extra_params="-f"
		;;
		--help)
			usage
			exit 0
		;;
		*)
			die "Invalid option '${x}'"
		;;
	esac
done

validationlist="fkvalidator.sh"

error=0
for script in ${validationlist}; do
	"${dbutils}/${script}" -u ${USERNAME} -s ${SERVERNAME} -p ${PORT} -d ${DATABASE} ${LOGFILE:+-l "$LOGFILE"} -q ${extra_params} || error=1
done
exit ${error}
