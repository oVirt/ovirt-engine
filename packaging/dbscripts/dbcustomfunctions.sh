#!/bin/bash

insert_initial_data() {
    printf "Inserting data  ...\n"
    execute_file "insert_data.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
    printf "Inserting pre-defined roles ...\n"
    execute_file "insert_predefined_roles.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
}

set_defaults() {
    ME=$(basename $0)
    SERVERNAME="localhost"
    PORT="5432"
    DATABASE="engine"
    USERNAME="engine"
    VERBOSE=false
    LOGFILE="$ME.log"
    DBOBJECT_OWNER="engine"
    NOMD5="false"
    MD5DIR="$(pwd)"
    LC_ALL="C"
    export LC_ALL

    if [ -n "${ENGINE_PGPASS}" ]; then
        export PGPASSFILE="${ENGINE_PGPASS}"
    else
        export PGPASSFILE="/etc/ovirt-engine/.pgpass"
        if [ ! -r "${PGPASSFILE}" ]; then
            export PGPASSFILE="${HOME}/.pgpass"
        fi
    fi
}

#refreshes views
refresh_views() {
    printf "Creating views...\n"
    execute_file "create_views.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
    execute_file "create_dwh_views.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
}

fn_db_set_dbobjects_ownership() {
    cmd="select c.relname \
         from   pg_class c join pg_roles r on r.oid = c.relowner join pg_namespace n on n.oid = c.relnamespace \
         where  c.relkind in ('r','v','S') \
         and    n.nspname = 'public' and r.rolname != '${DBOBJECT_OWNER}';"
    res=$(execute_command "${cmd}" engine ${SERVERNAME} ${PORT})
    if [ -n "${res}" ]; then
        cmd=""
        for tab in $(echo $res); do
            cmd=${cmd}"alter table $tab owner to ${DBOBJECT_OWNER}; "
        done
        if [ -n "${cmd}" ]; then
            echo -n "Changing ownership of objects in database '$DATABASE' to owner '$DBOBJECT_OWNER' ... "
            res=$(execute_command "${cmd}" engine ${SERVERNAME} ${PORT})
            if [ $? -eq 0 ]; then
                echo "completed successfully."
            else
                return 1
            fi
        fi
    fi
}
