#!/bin/sh

. "$(dirname "$(readlink -f "$0")")"/engine-prolog.sh
. "$(dirname "$(readlink -f "$0")")"/generate-pgpass.sh

usage() {
    cat << __EOF__
Usage $0:

    -a          - run analyze, update optimizer statistics
    -A          - run analyze only, only update optimizer stats, no vacuum
    -f          - do full vacuuming
    -t          - vacuum specific table
    -v          - verbose output

    -h --help   - this help message
__EOF__
}

MIXED_PARAMS_ERR="Can not mix -A and -f, use only one of them"

while getopts ":aAft:v" opt; do
    case $opt in
        a) ANALYZE=1
        ;;
        A) ANALYZE=
           ANALYZE_ONLY=1
            [[ -n $FULL ]] && die "$MIXED_PARAMS_ERR"
        ;;
        f) FULL=1
            [[ -n $ANALYZE_ONLY ]] && die "$MIXED_PARAMS_ERR"
        ;;
        t) TABLES="${TABLES} -t $OPTARG"
        ;;
        v) VERBOSE=1
        ;;
        \?) usage && exit
        ;;
        :) die "-$OPTARG requires an argument"
        ;;
    esac
done

cleanOrphanSchemas() {
    echo "cleaning orphan schemas..."
    tmpfile=$(mktemp /tmp/drop-orphan-system-schemas.XXXXXX.sql)
    su - postgres -c "psql  -Atc \"select 'drop schema if exists ' || nspname || ' cascade;' \
        from (select distinct nspname from pg_class join pg_namespace on (relnamespace=pg_namespace.oid) \
        where pg_is_other_temp_schema(relnamespace)) as foo\" ${ENGINE_DB_DATABASE}" > $tmpfile
    if [ -s $tmpfile ]; then
        chmod 777 $tmpfile
        su - postgres -c "psql -f ${tmpfile} ${ENGINE_DB_DATABASE}" >& /dev/null
    fi
}

# Cleaning up orphans schemas left by using temporary tables
# for details, see [1]
# [1] https://community.synopsys.com/s/article/Postgresql-log-LOG-autovacuum-found-orphan-temp-table-pg-temp-xxx-loblist-in-database-cim

[ -n "${FULL}" ] && CLEAN_ORPHAN_SCHEMAS=1

if [ -n "${CLEAN_ORPHAN_SCHEMAS}" ]; then
    if [ "${ENGINE_DB_HOST}" == "localhost" ]; then
        cleanOrphanSchemas
    else
	REMOTE=1
    fi
fi

# setups with 'trust' may have empty passwords
[[ -n $ENGINE_DB_PASSWORD ]] && generatePgPass

PGPASSFILE="${MYPGPASS}" vacuumdb \
${ANALYZE+-z} \
${ANALYZE_ONLY+-Z} \
${FULL+-f} \
${VERBOSE+-v -e} \
${TABLES+$TABLES} \
-h $ENGINE_DB_HOST \
-p $ENGINE_DB_PORT \
-U $ENGINE_DB_USER \
-d $ENGINE_DB_DATABASE \
-w

RET=$?
if [ "${RET}" -ne 0 -a -n "${REMOTE}" ]; then
	echo "For remote vacuuming if you got errors like 'permission denied for schema pg_temp_XX' please do the following:
	      1) log in into the remote database machine
	      2) run
	         psql  -U <db-admin-role> -Atc \"select 'drop schema if exists ' || nspname || ' cascade;'
                 from (select distinct nspname from pg_class join pg_namespace on (relnamespace=pg_namespace.oid)
                 where pg_is_other_temp_schema(relnamespace)) as foo\" ${ENGINE_DB_DATABASE} > <temporary file>
              3) run
	         psql  -U <db-admin-role> -f <temporary file>
              4) try to run engine-vacuum again"
fi
exit "${RET}"
