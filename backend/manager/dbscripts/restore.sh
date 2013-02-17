#!/bin/bash

################################################################################
# restore script based of generated sql by backup.sh
################################################################################

#include db general functions
pushd $(dirname ${0})>/dev/null
source ./dbfunctions.sh
source ./dbcustomfunctions.sh

#setting defaults
set_defaults

usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME] [-p PORT] -u USERNAME -d DATABASE -f FILE [-r] [-o] \n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database port for the database       (def. ${PORT})\n"
    printf "\t-u USERNAME   - The username for the database.\n"
    printf "\t-d DATABASE   - The database name, this must match the db name recorded in the backup file.\n"
    printf "\t-f File       - Backup file name to restore from. ${FILE}\n"
    printf "\t-r            - Remove existing database with same name\n"
    printf "\t-o            - Omit upgrade step\n"
    printf "\t-h            - This help text.\n"
    printf "\n"
    printf "for more options please run pg_restore --help\n"
    printf "\nThe recommended way for restoring your database is.\n"
    printf "\t1) Backup current database with backup.sh\n"
    printf "\t2) Run restore.sh and give new database instance name as the target\n"
    printf "\t3) Edit JBOSS standalone.xml to run the new restored database instance\n"
    printf "\t4) Verify that all tasks in the application are completed\n"
    printf "\t5) Restart JBOSS\n"
    popd>/dev/null
    exit 0
}


while getopts hs:d:u:p:l:f:ro option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        u) USERNAME=$OPTARG;;
        d) DATABASE=$OPTARG;;
        f) FILE=$OPTARG;;
        r) REMOVE_EXISTING=true;;
        o) OMIT_UPGRADE=true;;
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
        dropdb  -h ${SERVERNAME} -p ${PORT} -U ${USERNAME} ${DATABASE}
    fi
fi

echo "Restore of database $DATABASE from $FILE started..."
psql  -h ${SERVERNAME} -p ${PORT} -U ${USERNAME} -f ${FILE}

if [ $? -eq 0 ];then
    echo "Restore of database $DATABASE from $FILE completed."
     if [ ! -n "${OMIT_UPGRADE}" ]; then
         echo "Upgrading restored database..."
         ./upgrade.sh -s ${SERVERNAME} -p ${PORT} -d ${DATABASE} -u ${USERNAME} -c
     fi
    popd>/dev/null
    exit 0
else
    usage
    exit 1
fi
