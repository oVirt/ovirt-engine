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

while getopts :hs:d:u:p:l:f:m:gv option; do
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

printf "Creating the database: ${DATABASE}\n"

#try to drop the database first (if exists)
dropdb --username=${USERNAME} --host=${SERVERNAME} --port=${PORT} ${DATABASE} -e > /dev/null
createdb --username=${USERNAME} --host=${SERVERNAME} --port=${PORT} ${DATABASE} -e -E UTF8 --lc-collate en_US.UTF8  --lc-ctype en_US.UTF8 -T template0 > /dev/null
if [ $? -ne 0 ]
    then
      printf "Failed to create database ${DATABASE}\n"
      popd>/dev/null
      exit 1;
fi

if [ "${NOMD5}" = "true" ]; then
    if ! ./create_schema.sh -s "${SERVERNAME}" -p "${PORT}" -d "${DATABASE}" -u "${USERNAME}" -l "${LOGFILE}" -g; then
          printf "Failed to create schema for database ${DATABASE}\n"
          popd>/dev/null
          exit 1;
    fi
else
    if ! ./create_schema.sh -s "${SERVERNAME}" -p "${PORT}" -d "${DATABASE}" -u "${USERNAME}" -l "${LOGFILE}" -m "${MD5DIR}"; then
          printf "Failed to create schema for database ${DATABASE}\n"
          popd>/dev/null
          exit 1;
    fi
fi

popd>/dev/null
exit $?
