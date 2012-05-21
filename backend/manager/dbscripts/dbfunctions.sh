#!/bin/bash

# $1 - the command to execute
# $2 - the database to use
# $3 - db hostname  (default 'localhost' or '')
# $4 - db port (default '5432')
execute_command () {
    local command=${1}
    local dbname=${2}
    local dbhost=${3}
    local dbport=${4}
    local filename=$(mktemp)

    printf "${command}\n" > $filename

    execute_file $filename $dbname $dbhost $dbport
}

# $1 - the file to execute
# $2 - the database to use
# $3 - db hostname  (default 'localhost' or '')
# $4 - db port (default '5432')
execute_file () {
    local filename=${1}
    local dbname=${2}
    local dbhost=${3}
    local dbport=${4}
    local ret_instead_exit=${5}
    # tuples_only - supress header (column names) and footer  (rows affected) from output.
    # ON_ERROR_STOP - stop on error.
    local cmdline="psql --pset=tuples_only=on --set ON_ERROR_STOP=1"
    cmdline="${cmdline} --file=${filename} "

    if [[ -n "${dbname}" ]]; then
        cmdline="${cmdline} --dbname=${dbname} "
    fi

    if [[ -n "${USERNAME}" ]]; then
        cmdline="${cmdline} --username=${USERNAME} "
    fi

    if [[ -n "${dbhost}" ]]; then
        cmdline="${cmdline} --host=${dbhost} "
        if [[ -n "${dbport}" ]]; then
           cmdline="${cmdline} --port=${dbport} "
        fi
    fi


    if $VERBOSE; then
        cmdline="${cmdline} --echo-all "
    fi

    if [[ -n "${LOGFILE}" ]]; then
	cmdline="${cmdline} --log-file=${LOGFILE} "
    fi

    eval $cmdline
    # save last command return value
    retval=$?
    # exit script if command fails.
    if [ $retval -ne 0 -a -z "${ret_instead_exit}" ]; then
        exit $retval
    fi

    return $retval
}

#drops views before upgrade or refresh operations
drop_views() {
# common stored procedures are executed first (for new added functions to be valid)
execute_file "common_sp.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
    CMD="select * from generate_drop_all_views_syntax();"
    execute_command "$CMD" ${DATABASE} ${SERVERNAME} ${PORT} > drop_all_views.sql
    execute_file "drop_all_views.sql" ${DATABASE} ${SERVERNAME} ${PORT}> /dev/null
    \rm -f drop_all_views.sql
}

#drops sps before upgrade or refresh operations
drop_sps() {
# common stored procedures are executed first (for new added functions to be valid)
execute_file "common_sp.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
    CMD="select * from generate_drop_all_functions_syntax();"
    execute_command "$CMD"  ${DATABASE} ${SERVERNAME} ${PORT} > drop_all_functions.sql
    execute_file "drop_all_functions.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
    \rm -f drop_all_functions.sql
    # recreate generic functions
    execute_file "create_functions.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
}

#refreshes sps
refresh_sps() {
    printf "Creating stored procedures...\n"
    for sql in $(ls *sp.sql); do
        printf "Creating stored procedures from $sql ...\n"
        execute_file $sql ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
    done
    execute_file "common_sp.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
}

install_common_func() {
    # common stored procedures are executed first (for new added functions to be valid)
    execute_file "common_sp.sql" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
}

delete_async_tasks_and_compensation_data() {
    execute_file "delete_async_tasks_and_compensation_data.sql" ${DATABASE} ${SERVERNAME} ${PORT}> /dev/null
}

run_pre_upgrade() {
    #Dropping all views & sps
    drop_views
    drop_sps
    install_common_func
    #run pre upgrade scripts
    for file in $(ls -1 upgrade/pre_upgrade/*.sql); do
       echo "Running pre-upgrade script $file ..."
       execute_file $file ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
    done
    if [[ -n "${CLEAN_TASKS}" ]]; then
       echo "Cleaning tasks metadata..."
       delete_async_tasks_and_compensation_data
    fi
}

run_post_upgrade() {
    #Refreshing  all views & sps
    refresh_views
    refresh_sps
}

set_version() {
    execute_file upgrade/03_00_0000_add_schema_version.sql ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
    if [  -n "${VERSION}" ]; then
        CMD="update schema_version set current=true where version=trim('${VERSION}');"
        execute_command "${CMD}" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
    fi
}

get_current_version() {
    echo "select version from schema_version where current = true order by id LIMIT 1;" |
                    psql -U ${USERNAME} --pset=tuples_only=on ${DATABASE} -h ${SERVERNAME} -p ${PORT}
}

get_installed_version() {
    local cheksum=${1}
    echo "select version from schema_version where checksum = '${cheksum}' and state = 'INSTALLED';" |
                    psql -U ${USERNAME} --pset=tuples_only=on ${DATABASE} -h ${SERVERNAME} -p ${PORT}
}

get_last_installed_id() {
    echo "select max(id) from schema_version where state in ('INSTALLED','SKIPPED')" | psql -U ${USERNAME} --pset=tuples_only=on ${DATABASE} -h ${SERVERNAME} -p ${PORT}
}
set_last_version() {
    id=$(get_last_installed_id)
    CMD="update schema_version set current=(id=$id);"
    execute_command "${CMD}" ${DATABASE} ${SERVERNAME} ${PORT}> /dev/null
}

get_db_time(){
    echo "select now();" | psql -U ${USERNAME} --pset=tuples_only=on ${DATABASE} -h ${SERVERNAME} -p ${PORT}
}

is_view_or_sp_changed() {
    md5sum create_*views.sql *_sp.sql upgrade/*.sql upgrade/pre_upgrade/*.sql > .scripts.md5.tmp
    diff -s -q .scripts.md5 .scripts.md5.tmp >& /dev/null
    result=$?

    #  0 - identical , 1 - differ , 2 - error
    if [ $result -eq 0 ] ; then
        rm -f .scripts.md5.tmp
    else

        # there is a diff or md5 file does not exist
        mv -f .scripts.md5.tmp .scripts.md5
    fi
    return $result
}

validate_version_uniqueness() {
    prev=""
    for file in upgrade/??_??_????*.sql; do
        ver="${file:8:2}${file:11:2}${file:14:4}"
        if [ "$ver" = "$prev" ]; then
            echo "Operation aborted, found duplicate version : $ver"
            exit 1
        fi
        prev=$ver
    done
}

run_upgrade_files() {
    set_version
    res=$(find upgrade/ -name "*.sql" | wc -l)
    if [ $res -gt 0 ]; then
        state="FAILED"
        comment=""
        updated=0
        validate_version_uniqueness
        is_view_or_sp_changed

        # Checks if a view or sp file has been changed
        if [ $? -ne 0 ]; then
            echo "upgrade script detected a change in Config, View or Stored Procedure..."
            run_pre_upgrade
            updated=1
        fi

        # get current version
        current=$(get_current_version)
        # we should remove leading blank (from select result) and zero in order not to treat number as octal
        last="${current:2:7}"
        disable_gaps_from="3010910"
        for file in upgrade/??_??_????*.sql; do
            before=$(get_db_time)
            checksum=$(md5sum $file | cut -d " " -f1)
            # upgrade/dd_dd_dddd* => dddddddd
	    ver="${file:8:2}${file:11:2}${file:14:4}"
            if [ "$ver" -gt "$current" ] ; then
                # we should remove leading zero in order not to treat number as octal
                xver="${ver:1:7}"
                # check for gaps in upgrade
                if [ "$ver" -gt "$disable_gaps_from" ]; then
                    if [ $(($xver - $last)) -gt 10 ]; then
                        set_last_version
                        echo "Illegal script version number ${ver},version should be in max 10 gap from last installed version: 0${last}"
                        echo "Please fix numbering to interval 0$(( $last + 1)) to 0$(( $last + 10)) and run the upgrade script."
                    exit 1
                    fi
                fi
                # check if script was already installed with other version name.
                installed_version=$(get_installed_version $checksum)
                if [[ -n "${installed_version}" ]]; then
                    echo "Skipping upgrade script $file, already installed by ${installed_version}"
                    state="SKIPPED"
                    after=$(get_db_time)
                    last=$xver
                    comment="Installed already by ${installed_version}"
                else
                    echo "Running upgrade script $file "
                    execute_file $file ${DATABASE} ${SERVERNAME} ${PORT} 1 > /dev/null
                    code=$?
                    if [ $code -eq 0 ]; then
                        state="INSTALLED"
                        after=$(get_db_time)
                        last=$xver
                        comment=""
                    else
                        set_last_version
                        exit $code
                    fi
                fi
                CMD="insert into schema_version(version,script,checksum,installed_by,started_at,ended_at,state,current,comment)
                     values (trim('$ver'),'$file','$checksum','${USERNAME}',
                     cast(trim('$before') as timestamp),cast(trim('$after') as timestamp),'$state',false,'$comment');"
                execute_command "${CMD}" ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
            fi
        done
        set_last_version

        # restore views & SPs if dropped
        if [ $updated -eq 1 ]; then
            run_post_upgrade
            # auto generate .schema file
            pg_dump -f .schema -F p -n public -s -U ${USERNAME} ${DATABASE} -h ${SERVERNAME} -p ${PORT}  >& /dev/null
        else
	    echo "database is up to date."
        fi
    fi
}

pg_version() {
    echo $(psql --version | head -1 | awk '{print $3}')
}

check_and_install_uuid_osspa_pg8() {
    if [ $1 ]; then
        psql -d ${DATABASE} -U ${USERNAME} -h ${SERVERNAME} -p ${PORT} -f "$1"
        return $?
    elif [ ! -f /usr/share/pgsql/contrib/uuid-ossp.sql ] ; then
        return 1
    else
        psql -d ${DATABASE} -U ${USERNAME} -h ${SERVERNAME} -p ${PORT} -f /usr/share/pgsql/contrib/uuid-ossp.sql
        return $?
    fi
}

check_and_install_uuid_osspa_pg9() {
    # Checks that the extension is installed
    CMD_CHECK_INSTALLED="SELECT COUNT(extname) FROM pg_extension WHERE extname='uuid-ossp';"
    UUID_INSTALLED=$(expr `execute_command "${CMD_CHECK_INSTALLED}" ${DATABASE} ${SERVERNAME} ${PORT}`)
    # Checks that the extension can be installed
    CMD_CHECK_AVAILABLE="SELECT COUNT(name) FROM pg_available_extensions WHERE name='uuid-ossp';"
    UUID_AVAILABLE=$(expr `execute_command "${CMD_CHECK_AVAILABLE}" ${DATABASE} ${SERVERNAME} ${PORT}`)

    # If uuid is not installed, check whether it's available and install
    if [ $UUID_INSTALLED -eq 1 ]; then
        return 0
    else
        if [ $UUID_AVAILABLE -eq 0 ]; then
            return 1
        else
            CMD="CREATE EXTENSION \"uuid-ossp\";"
            execute_command "${CMD}"  ${DATABASE} ${SERVERNAME} ${PORT} > /dev/null
            return $?
        fi
    fi
}

check_and_install_uuid_osspa() {

    if [ $(pg_version | egrep "^9.1") ]; then
        echo "Creating uuid-ossp extension..."
        check_and_install_uuid_osspa_pg9 $1
    else
        echo "adding uuid-ossp.sql from contrib..."
        check_and_install_uuid_osspa_pg8 $1
    fi

    if [ $? -ne 0 ]; then
        printf "\nThe uuid-ossp extension is not available."
        printf "\nIt is possible the 'postgresql-contrib' package was not installed.\n"
        printf "In order to install the package in Fedora please perform: "
        printf "yum install postgresql-contrib\n"
        printf "After installation is done, please run create_db.sh script again.\n"
        printf "\nAlternatively, specify the location of the file with -f parameter\n"
        exit 1
    fi
}
