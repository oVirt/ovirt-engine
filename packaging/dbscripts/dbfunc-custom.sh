
. "${DBFUNC_COMMON_DBSCRIPTS_DIR}/dbfunc-common.sh"

DBFUNC_DB_USER="${DBFUNC_DB_USER:-engine}"
DBFUNC_DB_DATABASE="${DBFUNC_DB_DATABASE:-engine}"

DBFUNC_CUSTOM_CLEAN_TASKS=

dbfunc_common_hook_init_insert_data() {
        # generate new UUIDs for default DC & Cluster
	local spid="00000002-0002-0002-0002-00000000021c"
	local gen_spid=$(dbfunc_get_psql_result "select uuid_generate_v1();")

	local clusterid="00000001-0001-0001-0001-0000000000d6"
	local gen_clusterid=$(dbfunc_get_psql_result "select uuid_generate_v1();")

	# Replace static UUIDs with generated ones
	sed -i "s/'${spid}'/'${gen_spid}'/g" "${DBFUNC_COMMON_DBSCRIPTS_DIR}"/data/*.sql
	sed -i "s/'${clusterid}'/'${gen_clusterid}'/g" "${DBFUNC_COMMON_DBSCRIPTS_DIR}"/data/*.sql

	# Apply changes to database
        for script in $(ls "${DBFUNC_COMMON_DBSCRIPTS_DIR}"/data/*insert_*.sql); do
	    echo "Inserting data from ${script} ..."
	    dbfunc_psql_die --file="${script}" > /dev/null
        done

	# Restore previous static UUIDs
	sed -i "s/'${gen_spid}'/'${spid}'/g" "${DBFUNC_COMMON_DBSCRIPTS_DIR}"/data/*.sql
	sed -i "s/'${gen_clusterid}'/'${clusterid}'/g" "${DBFUNC_COMMON_DBSCRIPTS_DIR}"/data/*.sql
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
