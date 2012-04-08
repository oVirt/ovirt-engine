#!/bin/bash

################################################################################
# restore script based of generated sql by backup.sh
################################################################################

#include db general functions
if [ -e ./dbfunctions.sh ]; then
    source ./dbfunctions.sh
    source ./dbcustomfunctions.sh
else
    printf "backup script should be run from database scripts directory\n"
    exit 1
fi

#setting defaults
set_defaults

usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME] [-p PORT] -u USERNAME -d DATABASE -f FILE [-r] \n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database port for the database       (def. ${PORT})\n"
    printf "\t-u USERNAME   - The username for the database.\n"
    printf "\t-d DATABASE   - The database name\n"
    printf "\t-f File       - Backup file name to restore from. ${FILE}\n"
    printf "\t-r            - Remove existing database with same name\n"
    printf "\t-h            - This help text.\n"
    printf "\n"
    printf "for more options please run pg_restore --help"

    exit 0
}


while getopts hs:d:u:p:l:f:r option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        u) USERNAME=$OPTARG;;
        d) DATABASE=$OPTARG;;
        f) FILE=$OPTARG;;
        r) REMOVE_EXISTING=true;;
        h) usage;;
    esac
done

if [[ ! -n "${USERNAME}" ||  ! -n "${DATABASE}"  ||  ! -n "${FILE}"  ]]; then
   usage
   exit 1
fi

cmd="select datname from pg_database where datname ilike '${DATABASE}';"
res=$(execute_command "${cmd}" template1 ${SERVERNAME} ${PORT})
res=`echo $res | sed "s@^ @@g"`

if [ "${res}" =  "${DATABASE}" ]; then
    if [ ! -n "${REMOVE_EXISTING}" ]; then
        echo "Database ${DATABASE} exists, please use -r to force removing it."
        exit 1
    else
        cmd="drop database ${DATABASE};"
        echo ${cmd} | psql  -h ${SERVERNAME} -p ${PORT} -U ${USERNAME}
    fi
fi

echo "Restore of database $DATABASE from $FILE started..."
psql  -h ${SERVERNAME} -p ${PORT} -U ${USERNAME} -f ${FILE}

if [ $? -eq 0 ];then
    echo "Restore of database $DATABASE from $FILE completed."
    exit 0
else
    usage
    exit 1
fi
