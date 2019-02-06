
. "${DBFUNC_COMMON_DBSCRIPTS_DIR}/dbfunc-common.sh"

DBFUNC_DB_USER="${DBFUNC_DB_USER:-engine}"
DBFUNC_DB_DATABASE="${DBFUNC_DB_DATABASE:-engine}"

DBFUNC_CUSTOM_CLEAN_TASKS=

dbfunc_common_hook_init_insert_data() {
        # generate new UUIDs for default DC & Cluster
	local spid="58cfb470-02f3-03d7-0386-0000000003bc"
	local gen_spid=$(dbfunc_get_psql_result "select uuid_generate_v1();")

	local clusterid="58cfb470-03b9-01d0-03b9-0000000001e7"
	local gen_clusterid=$(dbfunc_get_psql_result "select uuid_generate_v1();")

	for script in $(ls "${DBFUNC_COMMON_DBSCRIPTS_DIR}"/data/*insert_*.sql); do
		dbfunc_output "Inserting data from ${script} ..."
		cat "${script}" | \
			sed \
				-e "s/'${spid}'/'${gen_spid}'/g" \
				-e "s/'${clusterid}'/'${gen_clusterid}'/g" | \
			dbfunc_psql_die --file=- > /dev/null
	done
}

dbfunc_common_hook_pre_upgrade() {
	if [ -n "${DBFUNC_CUSTOM_CLEAN_TASKS}" ]; then
		dbfunc_output "Cleaning tasks metadata..."
		dbfunc_psql_die_v --file="${DBFUNC_COMMON_DBSCRIPTS_DIR}/delete_async_tasks_and_compensation_data.sql" > /dev/null
	fi
}

#refreshes views
dbfunc_common_hook_views_refresh() {
	dbfunc_output "Creating views..."
	dbfunc_psql_die_v --file="${DBFUNC_COMMON_DBSCRIPTS_DIR}/create_views.sql" > /dev/null
	dbfunc_psql_die_v --file="${DBFUNC_COMMON_DBSCRIPTS_DIR}/create_dwh_views.sql" > /dev/null
}

# Materilized views functions, override with empty implementation on DBs that not supporting that

dbfunc_common_hook_sequence_numbers_update() {
	dbfunc_psql_die_v --file="${DBFUNC_COMMON_DBSCRIPTS_DIR}/update_sequence_numbers.sql" > /dev/null
}
