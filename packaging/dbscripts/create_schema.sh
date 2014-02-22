#!/bin/sh

cd "$(dirname "$0")"
. ./dbcustomfunctions.sh

cleanup() {
    dbfunc_cleanup
}
trap cleanup 0
dbfunc_init

usage() {
    cat << __EOF__
Usage: $0 [options]

    -h            - This help text.
    -v            - Turn on verbosity                         (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output          (def. ${DBFUNC_LOGFILE})
    -s HOST       - The database servername for the database  (def. ${DBFUNC_DB_HOST})
    -p PORT       - The database port for the database        (def. ${DBFUNC_DB_PORT})
    -u USER       - The username for the database             (def. ${DBFUNC_DB_USER})
    -d DATABASE   - The database name                         (def. ${DBFUNC_DB_DATABASE})
    -m MD5DIR     - The directory for generated MD5 files     (def. ${DBFUNC_COMMON_MD5DIR})

__EOF__
}

while getopts hvl:s:p:u:d:m: option; do
    case "${option}" in
       \?) usage; exit 1;;
        h) usage; exit 0;;
        v) DBFUNC_VERBOSE=1;;
        l) DBFUNC_LOGFILE="${OPTARG}";;
        s) DBFUNC_DB_HOST="${OPTARG}";;
        p) DBFUNC_DB_PORT="${OPTARG}";;
        u) DBFUNC_DB_USER="${OPTARG}";;
        d) DBFUNC_DB_DATABASE="${OPTARG}";;
        m) DBFUNC_COMMON_MD5DIR="${OPTARG}";;
    esac
done

echo "user name is: '${DBFUNC_DB_USER}'"

dbfunc_common_language_create "plpgsql"

#set database min error level
dbfunc_psql_die --command="ALTER DATABASE \"${DBFUNC_DB_DATABASE}\" SET client_min_messages=ERROR;" > /dev/null

echo "Creating tables..."
dbfunc_psql_die --file="create_tables.sql" > /dev/null

echo "Creating functions..."
dbfunc_psql_die --file="create_functions.sql" > /dev/null

echo "Creating common functions..."
dbfunc_psql_die --file="common_sp.sql" > /dev/null

#inserting initial data
insert_initial_data

#remove checksum file in clean install in order to run views/sp creation
[ -n "${DBFUNC_COMMON_MD5DIR}" ] && rm -f "${DBFUNC_COMMON_MD5DIR}/.${DBFUNC_DB_DATABASE}.scripts.md5" > /dev/null 2>&1

# Running upgrade scripts
echo "Running upgrade scripts..."
run_upgrade_files
