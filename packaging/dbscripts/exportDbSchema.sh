#!/bin/bash
#include db general functions
pushd $(dirname ${0})>/dev/null
source ./dbfunctions.sh
source ./dbcustomfunctions.sh

#setting defaults
set_defaults
VERBOSE=false

usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME] [-p PORT] [-d DATABASE] [-u USERNAME] [-v]\n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database servername port for the database (def. ${PORT})\n"
    printf "\t-d DATABASE   - The database name                        (def. ${DATABASE})\n"
    printf "\t-u USERNAME   - The username for the database            (def. engine)\n"
    printf "\t-l LOGFILE    - The logfile for capturing output         (def. ${LOGFILE}\n"
    printf "\t-v            - Turn on verbosity (WARNING: lots of output)\n"
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

while getopts hs:d:u:p:l:v option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
        l) LOGFILE=$OPTARG;;
        v) VERBOSE=true;;
        h) ret=0 && usage;;
       \?) ret=1 && usage;;
    esac
done

CMD="select version from schema_version where current;"
VERSION=$(psql -w -U ${USERNAME} --pset=tuples_only=on ${DATABASE} -h ${SERVERNAME} -p ${PORT} -c "${CMD}" | sed 's/^ *//g')
FILE=".${DATABASE}.${VERSION}.schema"
pg_dump -f "${FILE}" -F p -n public -s -U ${USERNAME} ${DATABASE} -h ${SERVERNAME} -p ${PORT} $(${VERBOSE} && echo -v)

printf "Done.\n"
printf "Exported file is ${FILE}.\n"
popd>/dev/null
exit 0
