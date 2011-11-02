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
        exit $retval;
    fi

}

#drops views before upgrade or refresh operations
drop_views() {
# common stored procedures are executed first (for new added functions to be valid)
execute_file "common_sp.sql" ${DATABASE} > /dev/null
    printf "Dropping all existing views ...\n"
    CMD="select * from generate_drop_all_views_syntax();"
    execute_command "$CMD"  ${DATABASE}  > drop_all_views.sql
    execute_file "drop_all_views.sql" ${DATABASE} > /dev/null
    \rm -f drop_all_views.sql
}

#drops sps before upgrade or refresh operations
drop_sps() {
# common stored procedures are executed first (for new added functions to be valid)
execute_file "common_sp.sql" ${DATABASE} > /dev/null
    printf "Dropping all existing stored procedures ...\n"
    CMD="select * from generate_drop_all_functions_syntax();"
    execute_command "$CMD"  ${DATABASE}  > drop_all_functions.sql
    execute_file "drop_all_functions.sql" ${DATABASE} > /dev/null
    \rm -f drop_all_functions.sql
    # recreate generic functions
    execute_file "create_functions.sql" ${DATABASE} > /dev/null
}


#refreshes views
refresh_views() {
    printf "Refreshing views...\n"
    execute_file "create_views.sql" ${DATABASE} > /dev/null
}

#refreshes sps
refresh_sps() {
    printf "Refreshing the stored procedures...\n"
    for sql in $(ls *sp.sql); do
        printf "Refreshing stored procedures from $sql ...\n"
        execute_file $sql ${DATABASE} > /dev/null
    done
    execute_file "common_sp.sql" ${DATABASE} > /dev/null
}

#run upgrade files
run_upgrade_files() {
    res=$(find upgrade/ -name "*.sql" | wc -l)
    if [ $res -gt 0 ]; then
        for upgradeFile in $(ls upgrade/*.sql); do
            printf "Running upgrade script $upgradeFile ...\n"
            execute_file $upgradeFile ${DATABASE} > /dev/null
        done
    fi
}

pg_version() {
    echo $(psql --version | head -1 | awk '{print $3}')
}

check_and_install_uuid_osspa_pg8() {
    if [ -e /usr/share/pgsql/contrib/uuid-ossp.sql ] ; then
        psql -d ${DATABASE} -U ${USERNAME} -f /usr/share/pgsql/contrib/uuid-ossp.sql
        return $?
    else
        return 1
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
        printf "\nThe uuid-ossp extension is not available. It is possible the postgresql-contrib package was not installed\n"
        printf "In order to install the package please perform:\n"
        printf "\nyum provides postgresql-contrib\nThis will determine which package should be installed. For example, for fedora 14 it should be: postgresql-contrib-8.4.7-1.fc14.x86_64\n"
        printf "\nyum install package-name\nFor example, for fedora 14 it should be: yum install postgresql-contrib-8.4.7-1.fc14.x86_64\n"
        printf "After installation is done, please run create_db.sh script again\n"
        exit 1
    fi
}

