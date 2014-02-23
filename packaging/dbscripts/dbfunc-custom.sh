
. ./dbfunc-common.sh

DBFUNC_DB_USER="${DBFUNC_DB_USER:-engine}"
DBFUNC_DB_DATABASE="${DBFUNC_DB_DATABASE:-engine}"

dbfunc_common_hook_init_insert_data() {
    echo "Inserting data..."
    dbfunc_psql_die --file="insert_data.sql" > /dev/null
    echo "Inserting pre-defined roles..."
    dbfunc_psql_die --file="insert_predefined_roles.sql" > /dev/null
}

#refreshes views
dbfunc_common_hook_views_refresh() {
    echo "Creating views..."
    dbfunc_psql_die --file="create_views.sql" > /dev/null
    dbfunc_psql_die --file="create_dwh_views.sql" > /dev/null
}

# Materilized views functions, override with empty implementation on DBs that not supporting that

dbfunc_common_hook_materialized_views_install() {
    dbfunc_psql_die --file="materialized_views_sp.sql" > /dev/null
}

dbfunc_common_hook_materialized_views_drop() {
    echo "Dropping materialized views..."
    dbfunc_psql_die --command="select DropAllMaterializedViews();" > /dev/null
}

dbfunc_common_hook_materialized_viewsrefresh_() {
    echo "Refreshing materialized views..."
    dbfunc_psql_die --command="select RefreshAllMaterializedViews(true);" > /dev/null
}

dbfunc_common_hook_sequence_numbers_update() {
    dbfunc_psql_die --file="update_sequence_numbers.sql" > /dev/null
}
