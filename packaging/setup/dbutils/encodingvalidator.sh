#!/bin/sh
###############################################################################################################
# The purpose of this utility is to find not UTF8 template1 encoding , display it and enable to fix it
# Only support may access this utility with care
# Use the -f flag to fix the problem by removing and recreating template1 with UTF8 encoding.
# Running this utility without the -f flag will only report the default encoding for template1.
# It is highly recomended to backup the database before using this utility.
###############################################################################################################

#include db general functions
cd "$(dirname "${0}")"
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
    -f            - Fix the template1 database encoding to be UTF8.
    -q            - Quiet operation: do not ask questions, assume 'yes' when fixing.

__EOF__
}

FIXIT=
QUIET=

while getopts hvl:s:p:u:fq option; do
    case $option in
       \?) usage; exit 1;;
        h) usage; exit 0;;
        v) VERBOSE=true;;
        l) LOGFILE="${OPTARG}";;
        s) SERVERNAME="${OPTARG}";;
        p) PORT="${OPTARG}";;
        u) USERNAME="${OPTARG}";;
        f) FIXIT=1;;
        q) QUIET=1;;
    esac
done

[ -n "${USERNAME}" ] || die "Please specify user"

run() {
  local command="${1}"
  local db="${2}"
  psql --pset=tuples_only=on -w -U "${USERNAME}" -h "${SERVERNAME}" -p "${PORT}" -c "${command}" "${db}"  > /dev/null
}

get() {
    local CMD="SELECT pg_encoding_to_char(encoding) FROM pg_database WHERE datname = 'template1';"
    local encoding="$(psql --pset=tuples_only=on -w -U ${USERNAME} -h ${SERVERNAME} -p ${PORT} -c "${CMD}"  template1)"
    echo "${encoding}" | sed -e 's/^ *//g'
}

fix_template1_encoding() {
    #Allow connecting to template 0
    local CMD
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

encoding="$(get)"

if [ "${encoding}" = "UTF8" -o "${encoding}" = "utf8" ]; then
    echo "Database template1 has already UTF8 default encoding configured. nothing to do, exiting..."
    exit 0
fi

echo "Database template1 is configured with an incompatible encoding: ${encoding}"

[ -n "${FIXIT}" ] || die "Database is incompatible"

if [ -z "${QUIET}" ]; then
    echo "Caution, this operation should be used with care. Please contact support prior to running this command"
    echo "Are you sure you want to proceed? [y/n]"
    read answer

    [ "${answer}" = "y" ] || die "Please contact support for further assistance."
fi

fix_template1_encoding
