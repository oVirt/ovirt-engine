#!/bin/sh
###############################################################################################################
# The purpose of this utility is to find not UTF8 template1 encoding , display it and enable to fix it
# Only support may access this utility with care
# Use the -f flag to fix the problem by removing and recreating template1 with UTF8 encoding.
# Running this utility without the -f flag will only report the default encoding for template1.
# It is highly recomended to backup the database before using this utility.
###############################################################################################################

. "$(dirname "${0}")/dbfunc-base.sh"

cleanup() {
	dbfunc_cleanup
}
trap cleanup 0
dbfunc_init

DBFUNC_DB_DATABASE="template1"

usage() {
    cat << __EOF__
Usage: $0 [options]

    -h            - This help text.
    -v            - Turn on verbosity                         (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output          (def. ${DBFUNC_LOGFILE})
    -s HOST       - The database servername for the database  (def. ${DBFUNC_DB_HOST})
    -p PORT       - The database port for the database        (def. ${DBFUNC_DB_PORT})
    -u USER       - The username for the database             (def. ${DBFUNC_DB_USER})
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
		v) DBFUNC_VERBOSE=1;;
		l) DBFUNC_LOGFILE="${OPTARG}";;
		s) DBFUNC_DB_HOST="${OPTARG}";;
		p) DBFUNC_DB_PORT="${OPTARG}";;
		u) DBFUNC_DB_USER="${OPTARG}";;
		f) FIXIT=1;;
		q) QUIET=1;;
	esac
done

[ -n "${DBFUNC_DB_USER}" ] || die "Please specify user"

get() {
	dbfunc_psql_statement_parsable "
		select pg_encoding_to_char(encoding)
		from pg_database
		where datname = 'template1'
	"
}

fix_template1_encoding() {
	#Allow connecting to template 0
	DBFUNC_DB_DATABASE="template1"
	dbfunc_psql_die --command="update pg_database set datallowconn = true where datname = 'template0';"
	#Define template1 as a regular DB so we can drop it
	DBFUNC_DB_DATABASE="template0"
	dbfunc_psql_die --command="update pg_database set datistemplate = false where datname = 'template1';"
	#drop tempalte1
	dbfunc_psql_die --command="drop database template1;"
	#recreate template1 with same encoding as template0
	dbfunc_psql_die --command="create database template1 with template = template0;"
	#restore changed defaults for template1
	dbfunc_psql_die --command="update pg_database set datistemplate = true where datname = 'template1';"
	#restore changed defaults for template0
	DBFUNC_DB_DATABASE="template1"
	dbfunc_psql_die --command="update pg_database set datallowconn = false where datname = 'template0';"
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
