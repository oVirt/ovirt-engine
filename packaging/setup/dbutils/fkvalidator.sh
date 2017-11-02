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
. "$(dirname "$0")/dbfunc-base.sh"

cleanup() {
	exit_code="$?"

	dbfunc_cleanup

	# Drop fkvalidator procedures
	dbfunc_psql_die --file="$(dirname "$0")/fkvalidator_sp_drop.sql" > /dev/null

	exit "${exit_code}"
}
trap cleanup 0
dbfunc_init

usage() {
    cat << __EOF__
Usage: $0 [options]

    -h            - This help text.
    -v            - Turn on verbosity                         (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output          (def. ${DBFUNC_LOGFILE})
    -s HOST       - The database servername for the database  (def. ${DBFUNC_DB_HOST})
    -p PORT       - The database port for the database        (def. ${DBFUNC_DB_PORT})
    -u USER       - The username for the database             (def. ${DBFUNC_DB_USER})
    -d DATABASE   - The database name                         (def. ${DBFUNC_DB_DATABASE})
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
	local res
	if [ -n "${fix_it}" ]; then
		res="$(
			dbfunc_psql_statement_parsable "
				select fk_violation
				from fn_db_validate_fks(true, ${verbose:-0} != 0)
			"
		)"
	else
		res="$(
			dbfunc_psql_statement_parsable "
				select fk_violation, fk_status
				from fn_db_validate_fks(false, ${verbose:-0} != 0)
				where fk_status=1
			"
		)"
	fi
	local exit_code=$?

	if [ ${exit_code} -eq 0 -a -z "${res}" ]; then
		exit 0
	fi

	echo "${res}" 1>&2
	if [ ! -z "${res}" -a -z "${fix_it}" ]; then
		exit_code=1
	fi
	exit ${exit_code}
}

FIXIT=
QUIET=

while getopts hvl:s:p:u:d:fq option; do
	case $option in
		\?) usage; exit 1;;
		h) usage; exit 0;;
		v) DBFUNC_VERBOSE=1;;
		l) DBFUNC_LOGFILE="${OPTARG}";;
		s) DBFUNC_DB_HOST="${OPTARG}";;
		p) DBFUNC_DB_PORT="${OPTARG}";;
		d) DBFUNC_DB_DATABASE="${OPTARG}";;
		u) DBFUNC_DB_USER="${OPTARG}";;
		f) FIXIT=1;;
		q) QUIET=1;;
	esac
done

[ -n "${DBFUNC_DB_USER}" ] || die "Please specify user name"
[ -n "${DBFUNC_DB_DATABASE}" ] || die "Please specify database"

if [ -n "${FIXIT}" -a -z "${QUIET}" ]; then
	echo "Caution, this operation should be used with care. Please contact support prior to running this command"
	echo "Are you sure you want to proceed? [y/n]"
	read answer

	[ "${answer}" = "y" ] || die "Please contact support for further assistance."
fi

# Install fkvalidator procedures
dbfunc_psql_die --file="$(dirname "$0")/fkvalidator_sp.sql" > /dev/null
# Execute
validate_db_fks "${FIXIT}" "${DBFUNC_VERBOSE}"
