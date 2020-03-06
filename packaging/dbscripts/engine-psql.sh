#!/bin/sh
#################################################################################
# The purpose of this script is to be a wrapper for support data manipulations on the
# database done up to now by psql.
# The script supports all original psql flags and records the executed SQL and its
# results in /var/log/ovirt-engine/db-manual/$TIMESTAMP-engine-psql.log
# The scripts supports both original -c <sql command> and -f <file> as also
# their equivalent --command <sql command> and --file <file> and
# --command=<sql command> and --file=<file>
#################################################################################

# Load vars file:

. "$(dirname "$(dirname "$(readlink -f "$0")")")"/bin/engine-prolog.sh

LOGDIR="${ENGINE_LOG}/db-manual"
LOGFILE="${LOGDIR}/$(date +"%Y%m%d%H%M%S")-engine-psql.log"
FILE=""

usage() {
    psql --help | sed "s@psql@$(basename "$0")@g"
    exit 0
}

log() {
    local m="$1"
    printf "%s\n" "$(date) : ${m}" >> "${LOGFILE}"
}

# we have to log the file content
# supporting here all possible formats : -f <file> --file <file> and --file=<file>
parseArgs() {
    while [ -n "$1" ]; do
        local x="$1"
        local v="${x#*=}"
        shift
        case "${x}" in
            --file=*)
                FILE="${v}"
                ;;
            -f|--file)
                FILE="$1"
                shift
                ;;
            -help|--help)
                usage
                ;;
        esac
    done
}

# do this in function so we do not lose $@
parseArgs "$@"

log "Executing command : ${0} $*"

# Echo file content to the log file
if [ -n "${FILE}" ]; then
	[ -r "${FILE}" ] || die "${FILE} is unreadable."
	log "file content is:"
	cat "${FILE}" >> "${LOGFILE}"
fi

log "Output:"
export PGPASSWORD=${ENGINE_DB_PASSWORD}
msg="DB admin ran : psql $@ on $ENGINE_DB_HOST/${@: -1}"
# Log to /var/log/messages if exists
# (rsyslog not installed anymore by default in fedora)
logger "${msg}" >& /dev/null #Ignore errors
psql -U "${ENGINE_DB_USER}" -h "${ENGINE_DB_HOST}" -p "${ENGINE_DB_PORT}" -d "${ENGINE_DB_DATABASE}" "$@" 2>&1 | tee -a "${LOGFILE}"
ret=$?
log "result: ${ret}"

exit $ret
