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
    USERNAME=""
    VERBOSE=false
    LOGFILE="$ME.log"
    export PGPASSFILE="/etc/ovirt-engine/.pgpass"
}

#refreshes views
refresh_views() {
    printf "Creating views...\n"
    execute_file "create_views.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
    execute_file "create_dwh_views.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
}

