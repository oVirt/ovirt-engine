###############################################################################################################
# The purpose of this utility is to find not UTF8 template1 encoding , display it and enable to fix it
# Only support may access this utility with care
# Use the -f flag to fix the problem by removing and recreating template1 with UTF8 encoding.
# Running this utility without the -f flag will only report the default encoding for template1.
# It is highly recomended to backup the database before using this utility.
###############################################################################################################

#!/bin/bash
#include db general functions
pushd $(dirname ${0})>/dev/null
source ./common.sh

#setting defaults
set_defaults


usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME [-p PORT]] [-u USERNAME] [-l LOGFILE] [-q] [-f] [-v]\n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database  (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database port for the database        (def. ${PORT})\n"
    printf "\t-u USERNAME   - The username for the database             (def. engine)\n"
    printf "\t-l LOGFILE    - The logfile for capturing output          (def. ${LOGFILE})\n"
    printf "\t-f            - Fix the template1 database encoding to be UTF8.\n"
    printf "\t-q            - Quiet operation: do not ask questions, assume 'yes' when fixing.\n"
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

FIXIT=false
QUIET=false

while getopts hs:u:p:l:d:qfv option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        u) USERNAME=$OPTARG;;
        l) LOGFILE=$OPTARG;;
        f) FIXIT=true;;
        q) QUIET=true;;
        v) VERBOSE=true;;
        h) ret=0 && usage;;
       \?) ret=1 && usage;;
    esac
done

run() {
  command="${1}"
  db="${2}"
  psql --pset=tuples_only=on -w -U ${USERNAME} -h ${SERVERNAME} -p ${PORT} -c "${command}"  ${db}  > /dev/null
}

get() {
    CMD="SELECT pg_encoding_to_char(encoding) FROM pg_database WHERE datname = 'template1';"
    encoding=$(psql --pset=tuples_only=on -w -U ${USERNAME} -h ${SERVERNAME} -p ${PORT} -c "${CMD}"  template1)
    echo "${encoding}" | sed -e 's/^ *//g'
}

fix_template1_encoding() {
    #Allow connecting to template 0
    CMD="UPDATE pg_database SET datallowconn = TRUE WHERE datname = 'template0';"
    run "${CMD}" template1
    #Define template1 as a regular DB so we can drop it
    CMD="UPDATE pg_database SET datistemplate = FALSE WHERE datname = 'template1';"
    run "${CMD}" template0
    #drop tempalte1
    CMD="drop database template1;"
    run "${CMD}" template0
    #recreate template1 with same encoding as template0
    CMD="create database template1 with template = template0;"
    run "${CMD}" template0
    #restore changed defaults for template1
    CMD="UPDATE pg_database SET datistemplate = TRUE WHERE datname = 'template1';"
    run "${CMD}" template0
    #restore changed defaults for template0
    CMD="UPDATE pg_database SET datallowconn = FALSE WHERE datname = 'template0';"
    run "${CMD}" template1
}

if [[ "${FIXIT}" = "true" && "${QUIET}" == "false" ]]; then
    echo "Caution, this operation should be used with care. Please contact support prior to running this command"
    echo "Are you sure you want to proceed? [y/n]"
    read answer

    if [ "${answer}" != "y" ]; then
       echo "Please contact support for further assistance."
       popd>/dev/null
       exit 1
    fi
fi

encoding=$(get)

if [[ "${encoding}" = "UTF8" || "${encoding}" = "utf8" ]]; then
   echo "Database template1 has already UTF8 default encoding configured. nothing to do, exiting..."
   exit 0
elif [ "${FIXIT}" = "false" ]; then
   echo "Database template1 is configured with an incompatible encoding: ${encoding}"
   exit 1
fi

fix_template1_encoding

if [ $? -eq 0 ]; then
  echo "Operation completed successfully"
else
  echo "Operation failed"
fi

popd>/dev/null
exit $?
