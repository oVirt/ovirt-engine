#!/bin/sh

################################################################################
# Cleans DB by dropping all DB objects
################################################################################

#include db general functions
cd "$(dirname "$0")"
. ./dbfunc-custom.sh

cleanup() {
    dbfunc_cleanup
}
trap cleanup 0
dbfunc_init

usage() {
    cat << __EOF__
Usage: $0 [options]

    -h            - This help text.
    -v            - Turn on verbosity (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output         (def. ${DBFUNC_LOGFILE}
    -s HOST       - The database servername for the database (def. ${DBFUNC_DB_HOST})
    -p PORT       - The database port for the database       (def. ${DBFUNC_DB_PORT})
    -u USER       - The username for the database            (def. ${DBFUNC_DB_USER})
    -d DATABASE   - The database name                        (def. ${DBFUNC_DB_DATABASE})

__EOF__
}

while getopts hvl:s:p:u:d: option; do
    case "${option}" in
       \?) usage; exit 1;;
        h) usage; exit 0;;
        v) DBFUNC_VERBOSE=1;;
        l) DBFUNC_LOGFILE="${OPTARG}";;
        s) DBFUNC_DB_HOST="${OPTARG}";;
        p) DBFUNC_DB_PORT="${OPTARG}";;
        u) DBFUNC_DB_USER="${OPTARG}";;
        d) DBFUNC_DB_DATABASE="${OPTARG}";;
    esac
done

echo "Cleaning database..."
dbfunc_common_schema_drop
