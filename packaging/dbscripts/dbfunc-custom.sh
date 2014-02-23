
. "${DBFUNC_COMMON_DBSCRIPTS_DIR}/dbfunc-common.sh"

DBFUNC_DB_USER="${DBFUNC_DB_USER:-engine}"
DBFUNC_DB_DATABASE="${DBFUNC_DB_DATABASE:-engine}"

DBFUNC_CUSTOM_CLEAN_TASKS=

dbfunc_common_hook_init_insert_data() {
	echo "Inserting data..."
	dbfunc_psql_die --file="${DBFUNC_COMMON_DBSCRIPTS_DIR}/insert_data.sql" > /dev/null
	echo "Inserting pre-defined roles..."
	dbfunc_psql_die --file="${DBFUNC_COMMON_DBSCRIPTS_DIR}/insert_predefined_roles.sql" > /dev/null
}

dbfunc_common_hook_pre_upgrade() {
	if [ -n "${DBFUNC_CUSTOM_CLEAN_TASKS}" ]; then
		echo "Cleaning tasks metadata..."
		dbfunc_psql_die --file="${DBFUNC_COMMON_DBSCRIPTS_DIR}/delete_async_tasks_and_compensation_data.sql" > /dev/null
	fi
}

#refreshes views
dbfunc_common_hook_views_refresh() {
	echo "Creating views..."
	dbfunc_psql_die --file="${DBFUNC_COMMON_DBSCRIPTS_DIR}/create_views.sql" > /dev/null
	dbfunc_psql_die --file="${DBFUNC_COMMON_DBSCRIPTS_DIR}/create_dwh_views.sql" > /dev/null
}

# Materilized views functions, override with empty implementation on DBs that not supporting that

dbfunc_common_hook_materialized_views_install() {
	dbfunc_psql_die --file="${DBFUNC_COMMON_DBSCRIPTS_DIR}/materialized_views_sp.sql" > /dev/null
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
	dbfunc_psql_die --file="${DBFUNC_COMMON_DBSCRIPTS_DIR}/update_sequence_numbers.sql" > /dev/null
}
