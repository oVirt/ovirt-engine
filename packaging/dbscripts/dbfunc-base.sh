#DBFUNC_VERBOSE=
#DBFUNC_LOGFILE=
DBFUNC_DB_HOST="${DBFUNC_DB_HOST:-localhost}"
DBFUNC_DB_PORT="${DBFUNC_DB_PORT:-5432}"
#DBFUNC_DB_USER=
#DBFUNC_DB_DATABASE=
#DBFUNC_DB_PGPASSFILE=

PSQL="${PSQL:-psql}"
PG_DUMP="${PG_DUMP:-pg_dump}"
NULL=

die() {
	local m="$1"
	echo "FATAL: ${m}" >&2
	exit 1
}

dbfunc_init() {
	if [ -n "${DBFUNC_DB_PGPASSFILE}" ]; then
		export PGPASSFILE="${DBFUNC_DB_PGPASSFILE}"
		unset PGPASSWORD
	fi
}

dbfunc_cleanup() {
	:
}

dbfunc_output() {
	local m="$1"
	local outf=/dev/stdout
	local timestamp="$(date +"%Y-%m-%d %H:%M:%S,%3N%z")"
	[ -n "${DBFUNC_LOGFILE}" ] && outf="${DBFUNC_LOGFILE}"
	printf "%s\n" "${timestamp} ${m}" >> "${outf}"
}

dbfunc_psql_raw() {
	LC_ALL="C" "${PSQL}" \
		-w \
		--pset=tuples_only=on \
		${DBFUNC_LOGFILE:+--log-file="${DBFUNC_LOGFILE}"} \
		--host="${DBFUNC_DB_HOST}" \
		--port="${DBFUNC_DB_PORT}" \
		--username="${DBFUNC_DB_USER}" \
		--dbname="${DBFUNC_DB_DATABASE}" \
		"$@"
}

dbfunc_psql() {
	dbfunc_psql_raw \
		--set ON_ERROR_STOP=1 \
		${DBFUNC_VERBOSE:+--echo-all} \
		"$@"
}

dbfunc_psql_die() {
	dbfunc_psql "$@" || die "Cannot execute sql command: $*"
}

# These log/output calls to them. Do not use if the output is sql
# that is fed back to psql.
dbfunc_psql_v() {
	dbfunc_output "dbfunc_psql $*"
	dbfunc_psql_raw \
		--set ON_ERROR_STOP=1 \
		${DBFUNC_VERBOSE:+--echo-all} \
		"$@"
}

dbfunc_psql_die_v() {
	dbfunc_output "dbfunc_psql_die $*"
	dbfunc_psql "$@" || die "Cannot execute sql command: $*"
}

dbfunc_psql_allow_errors() {
	dbfunc_psql_raw \
		${DBFUNC_VERBOSE:+--echo-all} \
		"$@"
}

dbfunc_psql_statement_parsable() {
	local statement="$1"
	dbfunc_psql_raw \
		-c "copy (${statement}) to stdout with delimiter as '|';"
}

dbfunc_get_psql_result() {
	echo $(dbfunc_psql_raw -c "$@" | tr -d ' ')
}

#
# parse line escape, each field
# in own line
#
dbfunc_psql_statement_parse_line() {
	local NL="
"
	local line="$1"
	local ret=""

	[ -z "${line}" ] && return 0

	local escape=
	while [ -n "${line}" ]; do
		c="$(expr substr "${line}" 1 1)"
		line="$(expr substr "${line}" 2 $((${#line}+1)))"
		if [ -n "${escape}" ]; then
			escape=
			ret="${ret}${c}"
		else
			case "${c}" in
				\\) escape=1 ;;
				\|) ret="${ret}${NL}" ;;
				*) ret="${ret}${c}" ;;
			esac
		fi
	done
	echo "${ret}"
}

dbfunc_pg_dump() {
	LC_ALL="C" "${PG_DUMP}" \
		-w \
		--host="${DBFUNC_DB_HOST}" \
		--port="${DBFUNC_DB_PORT}" \
		--username="${DBFUNC_DB_USER}" \
		"$@" \
		"${DBFUNC_DB_DATABASE}"
}

dbfunc_pg_dump_die() {
	dbfunc_pg_dump "$@" || die "Cannot execute pg_dump command: $*"
}
