#!/bin/bash 
#include db general functions
source ./dbfunctions.sh
source ./dbcustomfunctions.sh

#setting defaults
set_defaults

usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME] [-d DATABASE] [-u USERNAME] [-v]\n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database (def. ${SERVERNAME})\n"
    printf "\t-d DATABASE   - The database name                        (def. ${DATABASE})\n"
    printf "\t-u USERNAME   - The username for the database.\n"
    printf "\t-l LOGFILE    - The logfile for capturing output         (def. ${LOGFILE}\n"
    printf "\t-f UUID    - The [optional] location of uuid-ossp.sql file\n"
    printf "\t-v            - Turn on verbosity (WARNING: lots of output)\n"
    printf "\t-h            - This help text.\n"
    printf "\n"

    exit 0
}

DEBUG () {
    if $VERBOSE; then
        printf "DEBUG: $*"
    fi
}

while getopts :hs:d:u:p:l:f:v option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
	l) LOGFILE=$OPTARG;;
        f) UUID=$OPTARG;;
        v) VERBOSE=true;;
        h) usage;;
    esac
done

printf "Creating the database: ${DATABASE}\n"
#try to drop the database first (if exists)
dropdb --username=${USERNAME} --host=${SERVERNAME} ${DATABASE} -e > /dev/null
createdb --username=${USERNAME} --host=${SERVERNAME} ${DATABASE} -e -E UTF8 > /dev/null
if [ $? -ne 0 ]
    then
      printf "Failed to create database ${DATABASE}\n"
      exit 1;
fi
createlang --dbname=${DATABASE} --echo --username=${USERNAME} plpgsql >& /dev/null
#set database min error level
CMD="ALTER DATABASE \"${DATABASE}\" SET client_min_messages=ERROR;"
execute_command "${CMD}"  ${DATABASE} > /dev/null
printf "Inserting UUID functions...\n"

echo user name is: ${USERNAME} 

check_and_install_uuid_osspa ${UUID}

printf "Creating tables...\n"
execute_file "create_tables.sql" ${DATABASE} > /dev/null

printf "Creating functions...\n"
execute_file "create_functions.sql" ${DATABASE} > /dev/null

printf "Creating common functions...\n"
execute_file "common_sp.sql" ${DATABASE} > /dev/null

#inserting initial data
insert_initial_data

#remove checksum file in clean install in order to run views/sp creation
rm -f .scripts.md5 >& /dev/null

# Running upgrade scripts
printf "Running upgrade scripts...\n"
run_upgrade_files

exit $?
