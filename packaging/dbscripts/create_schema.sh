#!/bin/sh

cd "$(dirname "$0")"
. ./dbfunctions.sh
. ./dbcustomfunctions.sh

#setting defaults
set_defaults

usage() {
    cat << __EOF__
Usage: $0 [options]

    -h            - This help text.
    -v            - Turn on verbosity                         (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output          (def. ${LOGFILE})
    -s SERVERNAME - The database servername for the database  (def. ${SERVERNAME})
    -p PORT       - The database port for the database        (def. ${PORT})
    -u USERNAME   - The username for the database             (def. engine)
    -d DATABASE   - The database name                         (def. ${DATABASE})
    -m MD5DIR     - The directory for generated MD5 files     (def. ${MD5DIR})
    -g NOMD5      - Do not generate MD55 for files            (def. ${NOMD5})

__EOF__
}

while getopts hvl:s:p:u:d:m:g option; do
    case "${option}" in
       \?) usage; exit 1;;
        h) usage; exit 0;;
        v) VERBOSE=true;;
        l) LOGFILE="${OPTARG}";;
        s) SERVERNAME="${OPTARG}";;
        p) PORT="${OPTARG}";;
        u) USERNAME="${OPTARG}";;
        d) DATABASE="${OPTARG}";;
        m) MD5DIR="${OPTARG}";;
        g) NOMD5=true;;
    esac
done

createlang -w --host="${SERVERNAME}" --port="${PORT}" --dbname="${DATABASE}" --echo --username="${USERNAME}" plpgsql > /dev/null 2>&1
#set database min error level
CMD="ALTER DATABASE \"${DATABASE}\" SET client_min_messages=ERROR;"
execute_command "${CMD}"  "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null

echo "user name is: '${USERNAME}'"

echo "Creating tables..."
execute_file "create_tables.sql" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null

echo "Creating functions..."
execute_file "create_functions.sql" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null

echo "Creating common functions..."
execute_file "common_sp.sql" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null

#inserting initial data
insert_initial_data

#remove checksum file in clean install in order to run views/sp creation
[ -n "${MD5DIR}" ] && rm -f "${MD5DIR}/.${DATABASE}.scripts.md5" > /dev/null 2>&1

# Running upgrade scripts
echo "Running upgrade scripts..."
run_upgrade_files
