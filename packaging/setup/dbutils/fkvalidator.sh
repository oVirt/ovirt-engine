#!/bin/sh
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
cd "$(dirname "$0")"
. ./common.sh

#setting defaults
set_defaults

usage() {
    cat << __EOF__
Usage: $0 [options]

    -h            - This help text.
    -v            - Turn on verbosity                         (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output          (def. ${LOGFILE})
    -s SERVERNAME - The database servername for the database  (def. ${SERVERNAME})
    -p PORT       - The database port for the database        (def. ${PORT})
    -u USERNAME   - The username for the database             (def. engine)
    -d DATABASE   - The database name                         (def. ${DATABASE})
    -f            - Fix the non consistent data by removing it from DB.
    -q            - Run in a quiet mode (don't ask questions).

__EOF__
}

# Validates DB FKs
# if fix_it is false , constriant violations are reported only
# if fix_it is true , constriant violations cause is removed from DB
validate_db_fks() {
    local fix_it=${1}
    local verbose=${2}
    local CMD
    if [ -n "${fix_it}" ]; then
        if [ -n "${verbose}" ]; then
            CMD="copy (select fk_violation from fn_db_validate_fks(true,true)) to stdout;"
        else
            CMD="copy (select fk_violation from fn_db_validate_fks(true,false)) to stdout;"
        fi
    else
        if [ -n "${verbose}" ]; then
            CMD="copy (select fk_violation,fk_status from fn_db_validate_fks(false,true) where fk_status=1) to stdout with csv;"
        else
            CMD="copy (select fk_violation,fk_status from fn_db_validate_fks(false,false) where fk_status=1) to stdout with csv;"
        fi
    fi
    local res="$(psql -w --pset=tuples_only=on --set ON_ERROR_STOP=1 -U ${USERNAME} -c "${CMD}" -h "${SERVERNAME}" -p "${PORT}" -L "${LOGFILE}" "${DATABASE}")"
    local exit_code=$?

    local out="$(echo "${res}" | cut -f1 -d,)"
    echo "${out}"
    if [ "${exit_code}" = "0" -a -z "${fix_it}" ]; then
        exit_code="$(echo "${res}" | cut -f2 -d, | tail -1)"
    fi
    exit ${exit_code}
}

FIXIT=
QUIET=

while getopts hvl:s:p:u:d:fq option; do
    case $option in
       \?) usage; exit 1;;
        h) usage; exit 0;;
        v) VERBOSE=true;;
        l) LOGFILE="${OPTARG}";;
        s) SERVERNAME="${OPTARG}";;
        p) PORT="${OPTARG}";;
        u) USERNAME="${OPTARG}";;
        d) DATABASE="${OPTARG}";;
        f) FIXIT=1;;
        q) QUIET=1;;
    esac
done

# Install fkvalidator procedures
psql -w -U "${USERNAME}" -h "${SERVERNAME}" -p "${PORT}" -f ./fkvalidator_sp.sql "${DATABASE}" > /dev/null

if [ -n "${FIXIT}" -a -z "${QUIET}" ]; then
    echo "Caution, this operation should be used with care. Please contact support prior to running this command"
    echo "Are you sure you want to proceed? [y/n]"
    read answer

    if [ "${answer}" = "n" ]; then
        echo "Please contact support for further assistance."
        exit 1
    fi
fi

validate_db_fks "${FIXIT}" "${VERBOSE}"
