#!/bin/bash

################################################################################
# restore script based of generated sql by backup.sh
################################################################################

#include db general functions
pushd $(dirname ${0})>/dev/null
source ./dbfunctions.sh
source ./dbcustomfunctions.sh

#setting defaults
set_defaults

usage() {
    printf "Usage: ${ME} [-h] [-s SERVERNAME] [-p PORT] -u USERNAME -d DATABASE -f FILE [-r] [-o] \n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database port for the database       (def. ${PORT})\n"
    printf "\t-u USERNAME   - The username for the database            (def. engine)\n"
    printf "\t-d DATABASE   - The database name, this must match the db name recorded in the backup file.\n"
    printf "\t-f File       - Backup file name to restore from. ${FILE}\n"
    printf "\t-r            - Remove existing database with same name\n"
    printf "\t-o            - Omit upgrade step\n"
    printf "\t-h            - This help text.\n"
    printf "\n"
    printf "for more options please run pg_restore --help\n"
    printf "\nThe recommended way for restoring your database is.\n"
    printf "\t1) Backup current database with backup.sh\n"
    printf "\t2) Drop existing DB with dropdb or use the -r flag.\n"
    printf "\t3) Create a new blank db with the same name with createdb.\n"
    printf "\t4) Run restore.sh and give new database instance name as the target\n"
    popd>/dev/null
    exit 0
}

restore_from_tar() {
    # Copy tar file to working dir
    name=$(basename $FILE)
    dir="/tmp/${name}_dir"
    mkdir "${dir}"
    chmod 777 "${dir}"
    # Check SELinux mode
    selinux_mode=$(getenforce |tr '[A-Z]' '[a-z]')
    if [ "${selinux_mode}" != "disabled" ]; then
        # Restoring SELinux default settings
        chcon -Rt postgresql_db_t ${dir}
        if [ $? -ne 0 ]; then
            echo "Failed to restore SELinux default settings for ${dir}."
            exit 5
        fi
    fi
    cp "${FILE}" "${dir}/${name}"
    pushd "${dir}"
    # Extracting the tar file to working dir
    tar xf "${name}" > /dev/null
    if [ $? -ne 0 ]; then
        echo "Failed to extract TAR content to working directory ${dir}."
        exit 6
    fi
    chmod 777 *
    # dropping all statements we don't need on a clean DB from teh restore.sql file
    sed -i -e '/^DROP /d' -e '/^CREATE SCHEMA/d' -e '/^ALTER TABLE ONLY public\./d' -e '/^ALTER FUNCTION public\.uuid_/d' -e '/^CREATE PROCEDURAL LANGUAGE plpgsql/d' -e '/^ALTER PROCEDURAL LANGUAGE plpgsql/d' -e 's/^CREATE FUNCTION uuid_/CREATE OR REPLACE FUNCTION uuid_/g' -e 's?/tmp?'`pwd`'?'  -e 's?\$\$PATH\$\$?'`pwd`'?' restore.sql

    psql -w -h ${SERVERNAME} -p ${PORT} -U ${USERNAME} -f restore.sql ${DATABASE}
    res=$?
    popd
    rm -rf "${dir}"
    return $res
}


while getopts hs:d:u:p:l:f:ro option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        u) USERNAME=$OPTARG;;
        d) DATABASE=$OPTARG;;
        f) FILE=$OPTARG;;
        r) REMOVE_EXISTING=true;;
        o) OMIT_UPGRADE=true;;
        h) usage;;
    esac
done

if [[ ! -n "${USERNAME}" ||  ! -n "${DATABASE}"  ||  ! -n "${FILE}"  ]]; then
   usage
   exit 1
fi

cmd="select datname from pg_database where datname ilike '${DATABASE}';"
res=$(execute_command "${cmd}" template1 ${SERVERNAME} ${PORT})
res=`echo $res | sed "s@^ @@g"`

if [ "${res}" =  "${DATABASE}" ]; then
    if [ ! -n "${REMOVE_EXISTING}" ]; then
        echo "Database ${DATABASE} exists, please use -r to force removing it."
        exit 1
    else
        dropdb  -h ${SERVERNAME} -p ${PORT} -U postgres ${DATABASE}
        if [ $? -ne 0 ]; then
            echo "Failed to drop database ${DATABASE}."
            exit 2
        fi
    fi
fi

echo "Restore of database $DATABASE from $FILE started..."
if file "${FILE}" | grep 'tar'; then
    createdb -h ${SERVERNAME} -p ${PORT} -U postgres ${DATABASE}
    # Creating the plpgsql language
    createlang --host=${SERVERNAME} --port=${PORT} --dbname=${DATABASE} --username=${USERNAME} plpgsql >& /dev/null
    restore_from_tar
else
    psql -w -h ${SERVERNAME} -p ${PORT} -U ${USERNAME} -f ${FILE}
fi

if [ $? -eq 0 ];then
    echo "Restore of database $DATABASE from $FILE completed."
     if [ ! -n "${OMIT_UPGRADE}" ]; then
         echo "Upgrading restored database..."
         ./upgrade.sh -s ${SERVERNAME} -p ${PORT} -d ${DATABASE} -u ${USERNAME} -c
     fi
    popd>/dev/null
else
    usage
    exit 3
fi

fn_db_set_dbobjects_ownership
if [ $? -ne 0 ]; then
    echo "An error occurred whilst changing the ownership of objects in the database."
    exit 4
fi
