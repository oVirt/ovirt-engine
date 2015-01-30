#!/bin/sh

if [ "$#" -ne 6 ]; then
  echo "Usage: $0 <database host> <database port> <database user> <database name> <data directory> <action = {change,restore}>" >&2
  exit 1
fi

DB_HOST="${1}"
DB_PORT="${2}"
DB_USER="${3}"
DB_NAME="${4}"
OUT_DIR="${5}"
ACTION="${6}"

SPID="00000002-0002-0002-0002-00000000021c"
CLUSTERID="00000001-0001-0001-0001-0000000000d6"

# Executes query specified as 1st parameter using psql
psql_query() {
    echo $(psql -w --host="${DB_HOST}" --port="${DB_PORT}" --username="${DB_USER}" --dbname="${DB_NAME}" --tuples-only -c "$1" | tr -d ' ')
}

if [ "${ACTION}" = "change" ] ; then
    NEW_SPID=$(psql_query "select uuid_generate_v1();")
    NEW_CLUSTERID=$(psql_query "select uuid_generate_v1();")
    sed -i "s/'${SPID}'/'${NEW_SPID}'/g" "${OUT_DIR}"/*.sql
    sed -i "s/'${CLUSTERID}'/'${NEW_CLUSTERID}'/g" "${OUT_DIR}"/*.sql
elif [ "${ACTION}" = "restore" ] ; then
    NEW_SPID=$(psql_query "select id from storage_pool limit 1;")
    NEW_CLUSTERID=$(psql_query "select vds_group_id from vds_groups limit 1;")
    sed -i "s/'${NEW_SPID}'/'${SPID}'/g" "${OUT_DIR}"/*.sql
    sed -i "s/'${NEW_CLUSTERID}'/'${CLUSTERID}'/g" "${OUT_DIR}"/*.sql
fi
