#!/bin/sh

if [ "$#" -ne 4 ]; then
  echo "Usage: $0 <database user> <database name> <data directory> <op = {change,restore}>" >&2
  exit 1
fi

user=$1
db=$2
dir=$3
op=$4

spid="00000002-0002-0002-0002-00000000021c"
clusterid="00000001-0001-0001-0001-0000000000d6"
if [ "${op}" = "change" ] ; then
    newspid=$(psql -U $user -c "select uuid_generate_v1();" --tuples-only $db | tr -d ' ')
    newclusterid=$(psql -U $user -c "select uuid_generate_v1();" --tuples-only $db | tr -d ' ')
    sed -i "s/'${spid}'/'${newspid}'/g" "${dir}"/*.sql
    sed -i "s/'${clusterid}'/'${newclusterid}'/g" "${dir}"/*.sql
elif [ "${op}" = "restore" ] ; then
    newspid=$(psql -U $user -c "select id from storage_pool limit 1;" --tuples-only $db | tr -d ' ')
    newclusterid=$(psql -U $user -c "select vds_group_id from vds_groups limit 1;" --tuples-only $db | tr -d ' ')
    sed -i "s/'${newspid}'/'${spid}'/g" "${dir}"/*.sql
    sed -i "s/'${newclusterid}'/'${clusterid}'/g" "${dir}"/*.sql
fi
