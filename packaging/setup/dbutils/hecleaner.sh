#!/bin/sh
###############################################################################################################
# The purpose of this utility is to remove hosted-engine entities from the engine DB in order to be
# able to restore an engine backup took on an hosted-engine env on a new hosted-engine env.
# The utility enables to
# Remove
#     The hosted-engine storage domain, its entities and the hosted-engine VM
#     All the hosted-engine hosts
###############################################################################################################

. "$(dirname "$0")/dbfunc-base.sh"

cleanup() {
	dbfunc_cleanup
}
trap cleanup 0
dbfunc_init

#Using two variables for sql commands.
CMD="";
REMOVE_HE_STORAGE_VM=
REMOVE_HE_HOSTS=
QUIET_MODE=


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
    -R            - Removes the hosted-engine storage domain, all its entities and the hosted-engine VM.
    -M            - Removes all the hosted-engine hosts.
    -q            - Quiet mode, do not prompt for confirmation.

__EOF__
}

while getopts hvl:s:p:u:d:qRM option; do
	case "${option}" in
		\?) usage; exit 1;;
		h) usage; exit 0;;
		v) DBFUNC_VERBOSE=1;;
		l) DBFUNC_LOGFILE="${OPTARG}";;
		s) DBFUNC_DB_HOST="${OPTARG}";;
		p) DBFUNC_DB_PORT="${OPTARG}";;
		u) DBFUNC_DB_USER="${OPTARG}";;
		d) DBFUNC_DB_DATABASE="${OPTARG}";;
		q) QUIET_MODE=1;;
		R) REMOVE_HE_STORAGE_VM=1;;
		M) REMOVE_HE_HOSTS=1;;
	esac
done

caution() {
	if [ -z "${QUIET_MODE}" ]; then
		# Highlight the expected results of selected operation.
		cat << __EOF__
$(tput smso) $1 $(tput rmso)
Caution, this operation should be used with care. Please contact support prior to running this command
Are you sure you want to proceed? [y/n]
__EOF__
		read answer
		[ "${answer}" = "y" ] || die "Please contact support for further assistance."
	fi
}

[ -n "${DBFUNC_DB_USER}" ] || die "Please specify user name"
[ -n "${DBFUNC_DB_DATABASE}" ] || die "Please specify database"

dbfunc_psql_die --command="select exists (select * from information_schema.tables where table_schema = 'public' and table_name = 'command_entities');" | grep "t"

if [ -n "${REMOVE_HE_STORAGE_VM}" ]; then
    caution "This will remove the hosted-engine storage domain, all its entities and the hosted-engine VM!!!"
    CMD="${CMD}SELECT DeleteHostedEngineStorageVM();"
fi
if [ -n "${REMOVE_HE_HOSTS}" ]; then
    caution "Removes all the hosted-engine hosts!!!"
    CMD="${CMD}SELECT DeleteHostedEngineHosts();"
fi

# Install hecleaner procedures
dbfunc_psql_die --file="$(dirname "$0")/hecleaner_sp.sql" > /dev/null

# Execute
dbfunc_psql_die --command="${CMD}"

# Drop hecleaner procedures
dbfunc_psql_die --file="$(dirname "$0")/hecleaner_sp_drop.sql" > /dev/null

