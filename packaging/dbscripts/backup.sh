#!/bin/bash

################################################################################
# backup script wrapper on top of Postgres pg_dump utility
################################################################################

#include db general functions
pushd $(dirname ${0})>/dev/null
source ./dbfunctions.sh
source ./dbcustomfunctions.sh

#setting defaults
set_defaults

usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME] [-p PORT] [-d DATABASE] [-l DIR] [-f FILENAME] -u USERNAME [-c] [-v] \n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database port for the database       (def. ${PORT})\n"
    printf "\t-d DATABASE   - The database name                        (def. ${DATABASE})\n"
    printf "\t-u USERNAME   - The username for the database            (def. engine)\n"
    printf "\t-v            - Turn on verbosity (WARNING: lots of output)\n"
    printf "\t-l DIR        - Backup file directory. ${DIR}\n"
    printf "\t-f FILENAME   - Backup file name. ${FILENAME}\n"
    printf "\t-c            - Backup each row as SQL insert statement.\n"
    printf "\t-h            - This help text.\n"
    printf "\n"
    printf "for more options please run pg_dump --help"
    popd>/dev/null
    exit 0
}

DEBUG () {
    if $VERBOSE; then
        printf "DEBUG: $*"
    fi
}

while getopts hs:d:u:p:l:f:cv option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
        l) DIR=$OPTARG;;
        f) FILENAME=$OPTARG;;
        c) COLUMN_INSERTS=true;;
        v) VERBOSE=true;;
        h) usage;;
    esac
done

if [[ ! -n "${USERNAME}" ]]; then
   usage
   exit 1
fi

file=""
column_inserts=""

if [ -n "${FILENAME}" ]; then
    file="${FILENAME}";
else
    file=${DATABASE}_`date`.sql
    file=`echo $file | sed "s@ @_@g"`
fi


if [ -n "${DIR}" ]; then
    file="${DIR}/${file}"
fi

if [  -n "${COLUMN_INSERTS}" ]; then
    column_inserts=" --column-inserts "
fi

cmd="pg_dump -C -E UTF8 ${column_inserts} --disable-dollar-quoting  --disable-triggers --format=p -h ${SERVERNAME} -p ${PORT} -U ${USERNAME}  -f ${file}  ${DATABASE}"

echo "Backup of database $DATABASE to $file started..."

if [  -n "${VERBOSE}" ]; then
    cmd="${cmd} -v"
fi

${cmd}

if [ $? -eq 0 ];then
    echo "Backup of database $DATABASE to $file completed."
    popd>/dev/null
    exit 0
else
    usage
    exit 1
fi
