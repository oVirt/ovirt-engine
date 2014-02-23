
. ./dbfunc-base.sh

#DBFUNC_COMMON_MD5DIR=

dbfunc_common_hook_init_insert_data() {
    return 0
}

dbfunc_common_hook_views_refresh() {
    return 0
}

dbfunc_common_hook_materialized_views_install() {
    return 0
}

dbfunc_common_hook_materialized_views_drop() {
    return 0
}

dbfunc_common_hook_materialized_viewsrefresh_() {
    return 0
}

dbfunc_common_hook_sequence_numbers_update() {
    return 0
}

#cleans db by dropping all objects
dbfunc_common_schema_drop() {
    dbfunc_psql_die --file="common_sp.sql" > /dev/null
    local statement
    statement="$(
        dbfunc_psql_die --command="select * from generate_drop_all_seq_syntax();"
        dbfunc_psql_die --command="select * from generate_drop_all_tables_syntax();"
        dbfunc_psql_die --command="select * from generate_drop_all_views_syntax();"
        dbfunc_psql_die --command="select * from generate_drop_all_functions_syntax();"
        dbfunc_psql_die --command="select * from generate_drop_all_user_types_syntax();"
    )" || exit 1
    dbfunc_psql_die --command="${statement}" > /dev/null
}

dbfunc_common_init_insert_data() {
    dbfunc_common_hook_init_insert_data
}

dbfunc_common_upgrade() {

    dbfunc_psql_die --file=upgrade/03_02_0000_set_version.sql > /dev/null

    local res="$(find upgrade/ -name '*.sql' -or -name '*.sh' | wc -l)"
    if [ "${res}" -gt 0 ]; then
        local state="FAILED"
        local comment=""
        local updated=0
        _dbfunc_common_validate_version_uniqueness
        if [ -z "${DBFUNC_COMMON_MD5DIR}" ] || ! _dbfunc_common_is_view_or_sp_changed; then
            echo "upgrade script detected a change in Config, View or Stored Procedure..."
            _dbfunc_common_run_pre_upgrade
            updated=1
        fi

        # get current version
        local current="$(_dbfunc_common_get_current_version)"
        # we should remove leading blank (from select result) and zero in order not to treat number as octal
        local last="$(expr substr "${current}" 3 7)"
        local files="$(_dbfunc_common_get_files "upgrade" 1)"
        local file
        for file in $(ls ${files} | sort); do
            local before="$(_dbfunc_common_get_db_time)"
            local checksum="$(md5sum "${file}" | cut -d " " -f1)"
            # upgrade/dd_dd_dddd* => dddddddd
            local ver="$(_dbfunc_common_get_file_version "${file}")"
            if [ "${ver}" -gt "${current}" ] ; then
                # we should remove leading zero in order not to treat number as octal
                local xver="$(expr substr "${ver}" 2 7)"
                # taking major revision , i.e 03010000=>301
                local xverMajor="$(expr substr "${xver}" 1 3)"
                local lastMajor="$(expr substr "${last}" 1 3)"

                # check for gaps in upgrade
                # check gaps only for identical major revisions
                if [ "${xverMajor}" -eq "${lastMajor}" ]; then
                    if [ $((${xver} - ${last})) -gt 10 ]; then
                       _dbfunc_common_set_last_version
                       die "Illegal script version number ${ver},version should be in max 10 gap from last installed version: 0${last}
Please fix numbering to interval 0$(( ${last} + 1)) to 0$(( ${last} + 10)) and run the upgrade script."
                    fi
                fi
                # check if script was already installed with other version name.
                local installed_version="$(_dbfunc_common_get_installed_version "${checksum}")"
                if [ -n "${installed_version}" ]; then
                    echo "Skipping upgrade script ${file}, already installed by ${installed_version}"
                    state="SKIPPED"
                    after="$(_dbfunc_common_get_db_time)"
                    last="${xver}"
                    comment="Installed already by ${installed_version}"
                else
                    # force pre upgrade to run in case no md5 change was
                    # found but we still upgrade, like in db restore.
                    if [ "${updated}" = 0 ]; then
                       _dbfunc_common_run_pre_upgrade
                       updated=1
                    fi
                    _dbfunc_common_run_required_scripts "${file}"
                    _dbfunc_common_run_file "${file}"
                    code=$?
                    if [ "${code}" -eq 0 ]; then
                        state="INSTALLED"
                        after=$(_dbfunc_common_get_db_time)
                        last=$xver
                        comment=""
                    else
                        _dbfunc_common_set_last_version
                        exit "${code}"
                    fi
                fi
                dbfunc_psql_die --command="
                    insert into schema_version(
                        version,
                        script,
                        checksum,
                        installed_by,
                        started_at,
                        ended_at,
                        state,
                        current,
                        comment
                    )
                    values (
                        trim('${ver}'),
                        '${file}',
                        '${checksum}',
                        '${DBFUNC_DB_USER}',
                        cast(trim('${before}') as timestamp),
                        cast(trim('${after}') as timestamp),
                        '${state}',
                        false,
                        '${comment}'
                    );
                " > /dev/null
            fi
        done
        _dbfunc_common_set_last_version

        # restore views & SPs if dropped
        if [ "${updated}" -eq 1 ]; then
            _dbfunc_common_run_post_upgrade
        else
            echo "database is up to date."
        fi
    fi
}

# gets the configuration value of the given option name and version.
# usage: <some variable>=get_config_value <name> <version>
dbfunc_common_config_get_value() {
    local option_name="$1"
    local version="$2"

    dbfunc_psql_statement_parse_line "$(
        dbfunc_psql_statement_parsable "
            select option_value
            from vdc_options
            where
                option_name='${option_name}' and
                version='${version}'
        "
    )"
}

#drops views before upgrade or refresh operations
dbfunc_common_views_drop() {
    # common stored procedures are executed first (for new added functions to be valid)
    dbfunc_psql_die --file="common_sp.sql" > /dev/null
    dbfunc_psql_die --command="select * from generate_drop_all_views_syntax();" | \
        dbfunc_psql_die > /dev/null
}

#drops sps before upgrade or refresh operations
dbfunc_common_sps_drop() {
    dbfunc_psql_die --file="common_sp.sql" > /dev/null
    local statement
    statement="$(
        dbfunc_psql_die --command="select * from generate_drop_all_functions_syntax();"
    )" || exit 1
    dbfunc_psql_die --command="${statement}" > /dev/null

    # recreate generic functions
    dbfunc_psql_die --file="create_functions.sql" > /dev/null
}

#refreshes sps
dbfunc_common_sps_refresh() {
    echo "Creating stored procedures..."
    local sql
    for sql in $(ls *sp.sql | sort); do
        echo "Creating stored procedures from ${sql}..."
        dbfunc_psql_die --file="${sql}" > /dev/null
    done
    dbfunc_psql_die --file="common_sp.sql" > /dev/null
}

#unlocks the given VM/Template and its disks or a given disk
#in case of VM/Template the id is the name, in case of a disk, the id is the disk UUID
dbfunc_common_entity_unlock() {
    local object_type="$1"
    local id="$2"
    local user="$3"
    local recursive="$4"
    [ -z "${recursive}" ] && recursive=false || recursive=true
    local CMD=""
    if [ "${object_type}" = "vm" -o "${object_type}" = "template" ]; then
        CMD="select fn_db_unlock_entity('${object_type}', '${id}', ${recursive});"
    elif [ "${object_type}" = "disk" ]; then
        CMD="select fn_db_unlock_disk('${id}');"
    elif [ "${object_type}" = "snapshot" ]; then
        CMD="select fn_db_unlock_snapshot('${id}');"
    else
        printf "Error: $* "
    fi

    if [ -n "${CMD}" ]; then
        echo "${CMD}"
        if dbfunc_psql --command="${CMD}"; then
            _dbfunc_common_log_unlock_entity ${object_type} ${id} ${user}
            echo "unlock ${object_type} ${id} completed successfully."
        else
            echo "unlock ${object_type} ${id} completed with errors."
        fi
    fi
}

#Displays locked entities
dbfunc_common_entity_query() {
    local object_type="$1"
    local LOCKED=2
    local TEMPLATE_LOCKED=1
    local IMAGE_LOCKED=15;
    local SNAPSHOT_LOCKED=LOCKED
    local CMD
    if [ "${object_type}" = "vm" ]; then
        dbfunc_psql_die --command="
            select
                vm_name as vm_name
            from
                vm_static a,
                vm_dynamic b
            where
                a.vm_guid = b.vm_guid and
                status = ${IMAGE_LOCKED};

            select
                vm_name as vm_name,
                image_group_id as disk_id
            from
                images a,
                vm_static b,
                vm_device c
            where
                a.image_group_id = c.device_id and
                b.vm_guid = c.vm_id and
                imagestatus = ${LOCKED} and
                entity_type ilike 'VM' and
                image_group_id in (
                    select device_id
                    from vm_device
                    where is_plugged
                );

            select
                vm_name as vm_name,
                snapshot_id as snapshot_id
            from
                vm_static a,
                snapshots b
            where
                a.vm_guid = b.vm_id and
                status ilike '${SNAPSHOT_LOCKED}';
        "
    elif [ "${object_type}" = "template" ]; then
        dbfunc_psql_die --command="
            select vm_name as template_name
            from vm_static
            where template_status = ${TEMPLATE_LOCKED};

            select
                vm_name as template_name,
                image_group_id as disk_id
            from
                images a,
                vm_static b,
                vm_device c
            where
                a.image_group_id = c.device_id and
                b.vm_guid = c.vm_id and
                imagestatus = ${LOCKED} and
                entity_type ilike 'TEMPLATE' and
                image_group_id in (
                    select device_id
                    from vm_device
                    where is_plugged
                );
        "
    elif [ "${object_type}" = "disk" ]; then
        dbfunc_psql_die --command="
            select
                vm_id as entity_id,
                disk_id
            from
                base_disks a,
                images b,
                vm_device c
            where
                a.disk_id = b.image_group_id and
                b.image_group_id = c.device_id and
                imagestatus = ${LOCKED} and
                is_plugged;
        "
    elif [ "${object_type}" = "snapshot" ]; then
        dbfunc_psql_die --command="
            select
                vm_id as entity_id,
                snapshot_id
            from
                snapshots a
            where
                status ilike '${SNAPSHOT_LOCKED}';
        "
    fi
}

dbfunc_common_language_create() {
    local lang="$1"

    if [ "$(
        dbfunc_psql_statement_parsable "
            select count(*)
            from pg_language
            where lanname='${lang}'
        "
    )" -eq 0 ]; then
        dbfunc_psql_die --command="create language '${lang}';" > /dev/null
    fi
}

_dbfunc_common_run_pre_upgrade() {
    #Dropping all views & sps
    dbfunc_common_views_drop
    dbfunc_common_sps_drop
    # common stored procedures are executed first (for new added functions to be valid)
    dbfunc_psql_die --file="common_sp.sql" > /dev/null
    #update sequence numers
    dbfunc_common_hook_sequence_numbers_update
    #run pre upgrade scripts
    _dbfunc_common_psql_statements_in_dir 'pre_upgrade'
    dbfunc_common_hook_materialized_views_install
    #drop materialized views to support views changesin upgrade
    #Materialized views are restored in the post_upgrade step
    dbfunc_common_hook_materialized_views_drop

    # TODO: move this to custom?
    if [ -n "${CLEAN_TASKS}" ]; then
       echo "Cleaning tasks metadata..."
       dbfunc_psql_die --file="delete_async_tasks_and_compensation_data.sql" > /dev/null
    fi
}

_dbfunc_common_run_post_upgrade() {
    #Refreshing  all views & sps & run post-upgrade scripts
    dbfunc_common_hook_views_refresh
    dbfunc_common_sps_refresh
    #Running post-upgrade scripts
    _dbfunc_common_psql_statements_in_dir 'post_upgrade'
    #run custom materialized views if exists
    custom_materialized_views_file="upgrade/post_upgrade/custom/create_materialized_views.sql"
    if [ -f "${custom_materialized_views_file}" ]; then
        echo "running custom materialized views from '${custom_materialized_views_file}'..."
        if ! dbfunc_psql --file="${custom_materialized_views_file}"; then
            #drop all custom views
            dbfunc_psql --command="select DropAllCustomMaterializedViews();" > /dev/null
            echo "Illegal syntax in custom Materialized Views, Custom Materialized Views were dropped."
        fi
    fi
    dbfunc_common_hook_materialized_viewsrefresh_
}

# Runs all the SQL scripts in directory upgrade/$1/
_dbfunc_common_psql_statements_in_dir() {
    local dir="$1"
    if [ -d "upgrade/${dir}" ]; then
        files="$(_dbfunc_common_get_files "upgrade/${dir}" 1)"
        for file in $(ls ${files} | sort); do
           _dbfunc_common_run_file "${file}"
        done
    fi
}

_dbfunc_common_run_required_scripts() {
    local script="$1"
    # check for helper functions that the script needs
    # source scripts must be defined in the first lines of the script
    local line
    while read line; do
        expr="$(echo "${line}" | cut -d " " -f1 | grep "\-\-#source")"
        [ -z "${expr}" ] && break
        local sql="$(echo "${line}" | cut -d " " -f2)"
        echo "${sql}" | grep -q "_sp.sql" || \
            die "invalid source file ${sql} in ${file}, source files must end with '_sp.sql'"
        echo "Running helper functions from '${sql}' for '${file}'"
        dbfunc_psql_die --file="${sql}" > /dev/null
    done < "${script}"
}

_dbfunc_common_run_file() {
    local file="$1"
    if [ -x "${file}" ]; then
        # delegate all DBFUNC_ vars in subshell
        echo "Running upgrade shell script '${file}'..."
        (
            eval "$(set | grep '^DBFUNC_' | sed 's/^\([^=]*\)=.*/export \1/')"
            "./${file}"
        )
    else
        echo "Running upgrade sql script '${file}'..."
        dbfunc_psql_die --file="${file}" > /dev/null
    fi
}

_dbfunc_common_get_current_version() {
    dbfunc_psql_statement_parsable "
        select version
        from schema_version
        where current = true
        order by id
        LIMIT 1
    "
}

_dbfunc_common_get_installed_version() {
    local cheksum="$1"
    dbfunc_psql_statement_parsable "
        select version
        from schema_version
        where
            checksum = '${cheksum}' and
            state = 'INSTALLED'
    "
}

_dbfunc_common_set_last_version() {
    local id="$(
        dbfunc_psql_statement_parsable "
            select max(id)
            from schema_version
            where state in ('INSTALLED','SKIPPED')
        "
    )"
    dbfunc_psql_die --command="
        update schema_version
        set current=(id=${id});
    " > /dev/null
}

_dbfunc_common_get_db_time(){
    dbfunc_psql_statement_parsable "select now()"
}

# gets a directory and required depth and return all sql & sh files
_dbfunc_common_get_files() {
    local dir="$1"
    local maxdepth="$2"
    find "${dir}" \
        -maxdepth "${maxdepth}" \
        -name '*.sql' -or -name '*.sh' | \
        sort
}

_dbfunc_common_is_view_or_sp_changed() {
    local files="$(_dbfunc_common_get_files "upgrade" 3)"
    local md5sum_file="${DBFUNC_COMMON_MD5DIR}/.${DBFUNC_DB_DATABASE}.scripts.md5"
    local md5sum_tmp_file="${md5sum_file}.tmp"
    md5sum ${files} create_*views.sql *_sp.sql > "${md5sum_tmp_file}"
    diff -s -q "${md5sum_file}" "${md5sum_tmp_file}" > /dev/null 2>&1
    result=$?

    #  0 - identical , 1 - differ , 2 - error
    if [ $result -eq 0 ] ; then
        rm -f "${md5sum_tmp_file}"
    else
        # there is a diff or md5 file does not exist
        mv -f "${md5sum_tmp_file}" "${md5sum_file}"
    fi
    return $result
}

_dbfunc_common_get_file_version() {
    local file="$1"
    basename "${file}" | sed -e 's#\(..........\).*#\1#' -e 's/_//g'
}

_dbfunc_common_validate_version_uniqueness() {
    local prev=""
    local files="$(_dbfunc_common_get_files "upgrade" 1)"
    local file
    for file in $(ls ${files} | sort) ; do
        local ver="$(_dbfunc_common_get_file_version "${file}")"
        [ "${ver}" != "${prev}" ] || die "Operation aborted, found duplicate version: ${ver}"
        prev="${ver}"
    done
}

#adds a record to audit_log in case of calling unlock_entity
_dbfunc_common_log_unlock_entity() {
    local object_type="$1"
    local id="$2"
    local user="$3"

    dbfunc_psql_die --command="
        insert into audit_log(
            log_time,
            log_type_name,
            log_type,
            severity,
            message
        )
        values(
            now(),
            'USER_RUN_UNLOCK_ENTITY_SCRIPT',
            2024,
            10,
            'System user ${user} run unlock_entity script on ${object_type} ${id} with db user ${DBUTILS_DB_USER}}'
        )
    "
}
