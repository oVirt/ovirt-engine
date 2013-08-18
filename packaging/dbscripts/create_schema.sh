#!/bin/bash
#include db general functions
pushd $(dirname ${0})>/dev/null
source ./dbfunctions.sh
source ./dbcustomfunctions.sh

#setting defaults
set_defaults

usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME [-p PORT]] [-d DATABASE] [-u USERNAME] [-l LOGFILE] [-g] [-m MD5DIR] [-v]\n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database  (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database port for the database        (def. ${PORT})\n"
    printf "\t-d DATABASE   - The database name                         (def. ${DATABASE})\n"
    printf "\t-u USERNAME   - The username for the database             (def. engine)\n"
    printf "\t-l LOGFILE    - The logfile for capturing output          (def. ${LOGFILE})\n"
    printf "\t-g NOMD5      - Do not generate MD55 for files (generated in dev env only) (def. ${NOMD5}\n"
    printf "\t-m MD5DIR     - The directory for generated MD5 files (generated in dev env only) (def. ${MD5DIR}\n"
    printf "\t-v            - Turn on verbosity                         (WARNING: lots of output)\n"
    printf "\t-h            - This help text.\n"
    printf "\n"
    popd>/dev/null
    exit $ret
}

DEBUG () {
    if $VERBOSE; then
        printf "DEBUG: $*"
    fi
}

while getopts :hs:d:u:p:l:m:gv option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
        l) LOGFILE=$OPTARG;;
        m) MD5DIR=$OPTARG;;
        g) NOMD5=true;;
        v) VERBOSE=true;;
        h) ret=0 && usage;;
       \?) ret=1 && usage;;
    esac
done

createlang -w --host=${SERVERNAME} --port=${PORT} --dbname=${DATABASE} --echo --username=${USERNAME} plpgsql >& /dev/null
#set database min error level
CMD="ALTER DATABASE \"${DATABASE}\" SET client_min_messages=ERROR;"
execute_command "${CMD}"  ${DATABASE} ${SERVERNAME} ${PORT}> /dev/null

echo user name is: ${USERNAME}

printf "Creating tables...\n"
execute_file "create_tables.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null

printf "Creating functions...\n"
drop_old_uuid_functions
execute_file "create_functions.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null

printf "Creating common functions...\n"
execute_file "common_sp.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null

#inserting initial data
insert_initial_data

#remove checksum file in clean install in order to run views/sp creation
rm -f .${DATABASE}.scripts.md5 >& /dev/null

# Running upgrade scripts
printf "Running upgrade scripts...\n"
run_upgrade_files

popd>/dev/null
exit $?
