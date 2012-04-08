#!/bin/bash

################################################################################
# backup script wrapper on top of Postgres pg_dump utility
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
    printf "Usage: ${ME} [-h] [-s SERVERNAME] [-p PORT] [-d DATABASE] [-l DIR] -u USERNAME [-v] \n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database port for the database       (def. ${PORT})\n"
    printf "\t-d DATABASE   - The database name                        (def. ${DATABASE})\n"
    printf "\t-u USERNAME   - The username for the database.\n"
    printf "\t-v            - Turn on verbosity (WARNING: lots of output)\n"
    printf "\t-l DIR        - Backup file directory. ${DIR}\n"
    printf "\t-h            - This help text.\n"
    printf "\n"
    printf "for more options please run pg_dump --help"

    exit 0
}

DEBUG () {
    if $VERBOSE; then
        printf "DEBUG: $*"
    fi
}

while getopts hs:d:u:p:l:f:v option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
        l) DIR=$OPTARG;;
        v) VERBOSE=true;;
        h) usage;;
    esac
done

if [[ ! -n "${USERNAME}" ]]; then
   usage
   exit 1
fi

file=${DATABASE}_`date`.sql
file=`echo $file | sed "s@ @_@g"`

if [ -n "${DIR}" ]; then
    file="${DIR}/${file}"
fi

cmd="pg_dump -C -E UTF8  --column-inserts --disable-dollar-quoting  --disable-triggers --format=p -h ${SERVERNAME} -p ${PORT} -U ${USERNAME}  -f ${file}  ${DATABASE}"
echo "Backup of database $DATABASE to $file started..."

if [  -n "${VERBOSE}" ]; then
    ${cmd} -v
else
    ${cmd}
fi

if [ $? -eq 0 ];then
    echo "Backup of database $DATABASE to $file completed."
    exit 0
else
    usage
    exit 1
fi
