#!/bin/bash
#include db general functions
pushd $(dirname ${0})
source ./dbfunctions.sh

SERVERNAME="localhost"
DATABASE="engine"
USERNAME="postgres"
PORT="5432"

usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME [-p PORT]] [-d DATABASE] [-u USERNAME] [-l LOGFILE] [-v]\n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database  (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database port for the database        (def. ${PORT})\n"
    printf "\t-d DATABASE   - The database name                         (def. ${DATABASE})\n"
    printf "\t-u USERNAME   - The admin username for the database.\n"
    printf "\t-l LOGFILE    - The logfile for capturing output          (def. ${LOGFILE})\n"
    printf "\t-v            - Turn on verbosity                         (WARNING: lots of output)\n"
    printf "\t-h            - This help text.\n"
    printf "\n"
    popd
    exit $ret
}


while getopts hs:d:u:p:l:f:v option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
        l) LOGFILE=$OPTARG;;
        v) VERBOSE=true;;
        f) UUID=$OPTARG;;
        h) ret=0 && usage;;
       \?) ret=1 && usage;;
    esac
done

printf "Running original create_db script...\n"
./create_db.sh   -s $SERVERNAME -p $PORT -d $DATABASE -u $USERNAME;
if [ $? -ne 0 ]
    then
      printf "Failed to create database ${DATABASE}\n"
      popd
      exit 1;
fi
printf "Setting development configuration values ...\n"
execute_file "config_devel.sql" ${DATABASE} ${SERVERNAME} ${PORT}> /dev/null
ret=$?
printf "Development setting done.\n"
popd
exit $?
