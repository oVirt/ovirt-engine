
. ./dbfunc-common.sh

DBFUNC_DB_USER="${DBFUNC_DB_USER:-engine}"
DBFUNC_DB_DATABASE="${DBFUNC_DB_DATABASE:-engine}"

insert_initial_data() {
    echo "Inserting data..."
    dbfunc_psql_die --file="insert_data.sql" > /dev/null
    echo "Inserting pre-defined roles..."
    dbfunc_psql_die --file="insert_predefined_roles.sql" > /dev/null
}

#refreshes views
refresh_views() {
    echo "Creating views..."
    dbfunc_psql_die --file="create_views.sql" > /dev/null
    dbfunc_psql_die --file="create_dwh_views.sql" > /dev/null
}

# Materilized views functions, override with empty implementation on DBs that not supporting that

install_materialized_views_func() {
    dbfunc_psql_die --file="materialized_views_sp.sql" > /dev/null
}

drop_materialized_views() {
    echo "Dropping materialized views..."
    dbfunc_psql_die --command="select DropAllMaterializedViews();" > /dev/null
}

refresh_materialized_views() {
    echo "Refreshing materialized views..."
    dbfunc_psql_die --command="select RefreshAllMaterializedViews(true);" > /dev/null
}

update_sequence_numbers() {
    dbfunc_psql_die --file="update_sequence_numbers.sql" > /dev/null
}
