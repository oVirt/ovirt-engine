#!/bin/sh

#include db general functions
dbutils=$(dirname ${0})

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
    exit 1
}

error=0

while [ -n "$1" ]; do
    x="$1"
    v="${x#*=}"
    shift
    case "${x}" in
        --log=*)
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
            dbname="${v}"
        ;;
        --fix*)
            extra_params="-f"
        ;;
        --help)
            usage
        ;;
        *)
            die "Invalid option '${x}'"
        ;;
    esac
done

validationlist="fkvalidator.sh"

for script in ${validationlist}; do
        $dbutils/${script} ${USERNAME} ${SERVERNAME} ${PORT} ${DATABASE} -q ${extra_params} || error=1
done
exit ${error}
