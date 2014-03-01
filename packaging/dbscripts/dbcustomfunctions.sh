
die() {
    local m="$1"
    echo "FATAL: ${m}" >&2
    exit 1
}

insert_initial_data() {
    echo "Inserting data..."
    execute_file "insert_data.sql" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null
    echo "Inserting pre-defined roles..."
    execute_file "insert_predefined_roles.sql" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null
}

set_defaults() {
    local ME="$(basename $0)"
    SERVERNAME="localhost"
    PORT="5432"
    DATABASE="engine"
    USERNAME="engine"
    VERBOSE=false
    LOGDIR="/var/log/ovirt-engine"
    if [ -d "${LOGDIR}" ]; then
        LOGFILE="${LOGDIR}/$ME.log"
    else
        LOGFILE="$ME.log"
    fi
    DBOBJECT_OWNER="engine"
    NOMD5="false"
    MD5DIR="$(pwd)"
    LC_ALL="C"
    export LC_ALL

    if [ -n "${ENGINE_PGPASS}" ]; then
        export PGPASSFILE="${ENGINE_PGPASS}"
        unset PGPASSWORD
    else
        export PGPASSFILE="/etc/ovirt-engine/.pgpass"
        if [ ! -r "${PGPASSFILE}" ]; then
            export PGPASSFILE="${HOME}/.pgpass"
        fi
    fi
}

#refreshes views
refresh_views() {
    echo "Creating views..."
    execute_file "create_views.sql" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null
    execute_file "create_dwh_views.sql" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null
}

# Materilized views functions, override with empty implementation on DBs that not supporting that

install_materialized_views_func() {
    execute_file "materialized_views_sp.sql" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null
}

drop_materialized_views() {
    echo "Dropping materialized views..."
    local CMD="select DropAllMaterializedViews();"
    execute_command "${CMD}" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null
}

refresh_materialized_views() {
    echo "Refreshing materialized views..."
    CMD="select RefreshAllMaterializedViews(true);"
    execute_command "${CMD}" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null
}

update_sequence_numbers() {
    execute_file "update_sequence_numbers.sql" "${DATABASE}" "${SERVERNAME}" "${PORT}" > /dev/null
}
