#!/bin/bash
###############################################################################################################
# The purpose of this utility is to find inconsistent data that violates FK, display it and enable to remove it
# Only support may access this utility with care
# It is mandatory to run this utility on the original database before a backup of the DB is taken for later
# restore purpose, since if the database is backed up with the corrupted data and the FK definition, the FK
# will fail creation when the database is restored.
# Use the -f flag to fix the problem by removing the data that caused the FK violation.
# Running this utility without the -f flag will only report the violations.
# Use the -f flag to fix the problem by removing the data caused to the FK violation.
# Sample Output:
# >fkvalidator.sh -u  postgres -d dbname
#  psql:/tmp/tmp.fmQ0Q7O6ic:1: NOTICE:  Constraint violation found in  weather (city)  ... (2 records)
#
# >fkvalidator.sh -u  postgres -d dbname -f
#  Caution, this operation should be used with care. Please contact support prior to running this command
#  Are you sure you want to proceed? [y/n]
#  y
#  psql:/tmp/tmp.8p8BXKVObk:1: NOTICE:  Fixing weather (city)  ... (2 records)
###############################################################################################################

#include db general functions
pushd $(dirname ${0})>/dev/null
source ./common.sh

#setting defaults
set_defaults


usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME [-p PORT]] [-d DATABASE] [-u USERNAME] [-l LOGFILE] [-f] [-q] [-v]\n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database  (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database port for the database        (def. ${PORT})\n"
    printf "\t-d DATABASE   - The database name                         (def. ${DATABASE})\n"
    printf "\t-u USERNAME   - The username for the database             (def. engine)\n"
    printf "\t-l LOGFILE    - The logfile for capturing output          (def. ${LOGFILE})\n"
    printf "\t-f            - Fix the non consistent data by removing it from DB.\n"
    printf "\t-v            - Turn on verbosity                         (WARNING: lots of output)\n"
    printf "\t-q            - Run in a quiet mode (don't ask questions).\n"
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

# Validates DB FKs
# if fix_it is false , constriant violations are reported only
# if fix_it is true , constriant violations cause is removed from DB
validate_db_fks() {
   local fix_it=${1}
   if [ "${fix_it}" = "true" ]; then
       CMD="copy (select fk_violation from fn_db_validate_fks(true)) to stdout;"
   else
       CMD="copy (select fk_violation,fk_status from fn_db_validate_fks(false) where fk_status=1) to stdout with csv;"
   fi
   res="$(psql -w --pset=tuples_only=on --set ON_ERROR_STOP=1 -U ${USERNAME} -c "${CMD}" -h "${SERVERNAME}" -p "${PORT}" "${DATABASE}")"
   exit_code=$?

   out="$(echo "${res}" | cut -f1 -d,)"
   if [ "${exit_code}" = "0" ]; then
       exit_code="$(echo "${res}" | cut -f2 -d, | head -1)"
   fi
   echo "${out}"
   exit ${exit_code}
}

FIXIT=false

while getopts hs:d:u:p:l:fqv option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
        l) LOGFILE=$OPTARG;;
        f) FIXIT=true;;
        v) VERBOSE=true;;
        q) QUIET=true;;
        h) ret=0 && usage;;
       \?) ret=1 && usage;;
    esac
done

# Install fkvalidator procedures
psql -w -U ${USERNAME} -h ${SERVERNAME} -p ${PORT} -f ./fkvalidator_sp.sql ${DATABASE} > /dev/null

if [[ "${FIXIT}" = "true" && ! "${QUIET}" = "true" ]]; then
    echo "Caution, this operation should be used with care. Please contact support prior to running this command"
    echo "Are you sure you want to proceed? [y/n]"
    read answer

    if [ "${answer}" = "n" ]; then
       echo "Please contact support for further assistance."
       popd>/dev/null
       exit 1
    fi
fi

validate_db_fks ${FIXIT}

popd>/dev/null
exit $?
