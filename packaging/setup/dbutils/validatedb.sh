#!/bin/sh

dbutils="$(dirname "${0}")"
. "${dbutils}/dbfunc-base.sh"

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
			USERNAME="-u ${v}"
		;;
		--host=*)
			SERVERNAME="-s ${v}"
		;;
		--port=*)
			PORT="-p ${v}"
		;;
		--database=*)
			DATABASE="-d ${v}"
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
	"${dbutils}/${script}" ${USERNAME} ${SERVERNAME} ${PORT} ${DATABASE} ${LOGFILE:+-l "$LOGFILE"} -q ${extra_params} || error=1
done
exit ${error}
