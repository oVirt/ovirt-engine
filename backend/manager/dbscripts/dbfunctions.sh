#!/bin/bash 

# $1 - the command to execute
# $2 - the database to use
execute_command () {
    local command=${1}
    local dbname=${2-}
    local filename=$(mktemp)

    printf "${command}\n" > $filename

    execute_file $filename $DATABASE
}

# $1 - the file to execute
# $2 - the database to use
execute_file () {
    local filename=${1}
    local dbname=${2-}
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
    if [ $retval -ne 0 ]
    then
        return $retval;
    fi

}

#drops views before upgrade or refresh operations
drop_views() {
# common stored procedures are executed first (for new added functions to be valid)
execute_file "common_sp.sql" ${DATABASE} > /dev/null
    CMD="select * from generate_drop_all_views_syntax();"
    execute_command "$CMD"  ${DATABASE}  > drop_all_views.sql
    execute_file "drop_all_views.sql" ${DATABASE} > /dev/null
    \rm -f drop_all_views.sql
}

#drops sps before upgrade or refresh operations
drop_sps() {
# common stored procedures are executed first (for new added functions to be valid)
execute_file "common_sp.sql" ${DATABASE} > /dev/null
    CMD="select * from generate_drop_all_functions_syntax();"
    execute_command "$CMD"  ${DATABASE}  > drop_all_functions.sql
    execute_file "drop_all_functions.sql" ${DATABASE} > /dev/null
    \rm -f drop_all_functions.sql
    # recreate generic functions
    execute_file "create_functions.sql" ${DATABASE} > /dev/null
}


#refreshes views
refresh_views() {
    printf "Creating views...\n"
    execute_file "create_views.sql" ${DATABASE} > /dev/null
}

#refreshes sps
refresh_sps() {
    printf "Creating stored procedures...\n"
    for sql in $(ls *sp.sql); do
        printf "Creating stored procedures from $sql ...\n"
        execute_file $sql ${DATABASE} > /dev/null
    done
    execute_file "common_sp.sql" ${DATABASE} > /dev/null
}

install_common_func() {
    # common stored procedures are executed first (for new added functions to be valid)
    execute_file "common_sp.sql" ${DATABASE} > /dev/null
}

run_pre_upgrade() {
    #Dropping all views & sps
    drop_views
    drop_sps
    install_common_func
}

run_post_upgrade() {
    #Refreshing  all views & sps
    refresh_views
    refresh_sps
}

set_version() {
    execute_file upgrade/03_00_0000_add_schema_version.sql ${DATABASE} > /dev/null
    if [  -n "${VERSION}" ]; then
        CMD="update schema_version set current=true where version=trim('${VERSION}');"
        execute_command "${CMD}" ${DATABASE} > /dev/null
    fi
}

get_current_version() {
    echo "select version from schema_version where current = true order by id LIMIT 1;" |
                    psql -U ${USERNAME} --pset=tuples_only=on ${DATABASE}
}

get_last_installed_id() {
    echo "select max(id) from schema_version where state = 'INSTALLED'" | psql -U ${USERNAME} --pset=tuples_only=on ${DATABASE}
}

set_last_version() {
    id=$(get_last_installed_id)
    CMD="update schema_version set current=(id=$id);"
    execute_command "${CMD}" ${DATABASE} > /dev/null
}

get_db_time(){
    echo "select now();" | psql -U ${USERNAME} --pset=tuples_only=on ${DATABASE}
}

is_view_or_sp_changed() {
    md5sum create_views.sql *_sp.sql upgrade/*.sql > .scripts.md5.tmp
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
    current=$(get_current_version)
    res=$(find upgrade/ -name "*.sql" | wc -l)
    if [ $res -gt 0 ]; then
        state="FAILED"
        updated=0
        validate_version_uniqueness
        is_view_or_sp_changed

        # Checks if a view or sp file has been changed
        if [ $? -ne 0 ]; then
            echo "upgrade script detected a change in a View or Stored Procedure..."
            run_pre_upgrade
            updated=1
        fi

	for file in upgrade/??_??_????*.sql; do
            before=$(get_db_time)
            checksum=$(md5sum $file | cut -d " " -f1)
            # upgrade/dd_dd_dddd* => dddddddd
	    ver="${file:8:2}${file:11:2}${file:14:4}"
	    if [ "$ver" -gt "$current" ] ; then
                echo "Running upgrade script $file "
                execute_file $file ${DATABASE} > /dev/null
                if [ $? -eq 0 ]; then
                    state="INSTALLED"
                    after=$(get_db_time)
                else
                    code=$?
                    set_last_version
                    exit $code
                fi
                CMD="insert into schema_version(version,script,checksum,installed_by,started_at,ended_at,state,current)
                     values (trim('$ver'),'$file','$checksum','${USERNAME}',
                     cast(trim('$before') as timestamp),cast(trim('$after') as timestamp),'$state',false);"
                execute_command "${CMD}" ${DATABASE} > /dev/null
            fi
        done
        set_last_version

        # restore views & SPs if dropped
        if [ $updated -eq 1 ]; then
            run_post_upgrade
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
        psql -d ${DATABASE} -U ${USERNAME} -f "$1"
        return $?
    elif [ ! -f /usr/share/pgsql/contrib/uuid-ossp.sql ] ; then
        return 1
    else
        psql -d ${DATABASE} -U ${USERNAME} -f /usr/share/pgsql/contrib/uuid-ossp.sql
        return $?
    fi
}

check_and_install_uuid_osspa_pg9() {
    CMD="SELECT name FROM pg_available_extensions WHERE name='uuid-ossp';"
    RES=`execute_command "${CMD}" ${DATABASE}`
    if [ ! "$RES" ]; then
        return 1
    else
        CMD="CREATE EXTENSION \"uuid-ossp\";"
        execute_command "${CMD}"  ${DATABASE} > /dev/null
        return $?
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

