#!/bin/bash

################################################################################
# restore script based of generated sql by backup.sh
################################################################################

#include db general functions
pushd $(dirname ${0}) > /dev/null
source ./dbfunctions.sh
source ./dbcustomfunctions.sh

#setting defaults
set_defaults

usage() {
    cat << __EOF__
Usage: ${ME} [-h] [-s SERVERNAME] [-p PORT] -u USERNAME -d DATABASE -f FILE [-o]
    This script must run with a DB engine user credentials.
    -s SERVERNAME - The database servername for the database (def. ${SERVERNAME}).
    -p PORT       - The database port for the database       (def. ${PORT}).
    -u USERNAME   - The username for the database            (def. engine).
    -d DATABASE   - The database name, this must match the db name recorded in the backup file.
    -f File       - Backup file name to restore from ${FILE}.
    -o            - Omit upgrade step.
    -h            - This help text.
    for more options please run pg_restore --help
    The recommended way for restoring your database is.
    1) Backup current database with backup.sh.
    2) Drop existing from root user by : su - postgres -c "psql -d template1 -c 'drop database engine;'"
    3) Create a new blank db with the same name by:  su - postgres -c "psql -d template1 -c 'create database engine owner engine;'"
    4) Run restore.sh and give new database instance name as the target.
__EOF__
	popd > /dev/null
        exit $ret
}

restore_from_tar() {
	# Copy tar file to working dir
	name=$(basename ${FILE})
	dir="$(mktemp -d /tmp/${name}_XXXX)"
	chmod 777 "${dir}"
	# Check SELinux mode
	selinux_mode=$(getenforce |tr '[A-Z]' '[a-z]')
	if [[ "${selinux_mode}" = "enforcing" ]]; then
		# Restoring SELinux default settings
		chcon -Rt postgresql_db_t "${dir}"
		if [[ $? -ne 0 ]]; then
			echo "Failed to restore SELinux default settings for ${dir}."
			exit 5
		fi
	fi
	cp "${FILE}" "${dir}/${name}"
	pushd "${dir}"
	# Extracting the tar file to working dir
	tar xf "${name}" > /dev/null
	if [[ $? -ne 0 ]]; then
		echo "Failed to extract TAR content to working directory ${dir}."
                popd
		exit 6
	fi
	chmod 777 *
	# dropping all statements we don't need on a clean DB from the restore.sql file
	sed -i -e '/^CREATE DATABASE /d' -e '/ALTER DATABASE /d' -e '/^DROP /d' -e '/^CREATE SCHEMA/d' -e '/^ALTER TABLE ONLY public\./d' -e '/^ALTER FUNCTION public\.uuid_/d' -e '/^CREATE PROCEDURAL LANGUAGE plpgsql/d' -e '/^ALTER PROCEDURAL LANGUAGE plpgsql/d' -e 's/^CREATE FUNCTION uuid_/CREATE OR REPLACE FUNCTION uuid_/g' -e 's?/tmp?'`pwd`'?'  -e 's?\$\$PATH\$\$?'`pwd`'?' restore.sql

	psql -w -h "${SERVERNAME}" -p "${PORT}" -U "${USERNAME}" -f restore.sql "${DATABASE}"
	res=$?
	popd
	rm -rf "${dir}"
	return $res
}

get_query_result() {
    local cmd=${1}
    local db=${2}
    res=$(execute_command "${cmd}" "${db}"  "${SERVERNAME}" "${PORT}")
    echo $res | sed "s@^ @@g"
}


while getopts hs:d:u:p:f:o option; do
	case $option in
		s) SERVERNAME=$OPTARG;;
		p) PORT=$OPTARG;;
		u) USERNAME=$OPTARG;;
		d) DATABASE=$OPTARG;;
		f) FILE=$OPTARG;;
		o) OMIT_UPGRADE=true;;
                h) ret=0 && usage;;
                \?) ret=1 && usage;;
	esac
done

if [[ -z "${USERNAME}" \
	|| -z "${DATABASE}"  \
	|| -z "${FILE}" ]]; then

	usage
fi

res=$(get_query_result "select datname from pg_database where datname ilike '${DATABASE}';" "template1")

if [[ "${res}" !=  "${DATABASE}" ]]; then
    echo "Database ${DATABASE} does not exist, please create an empty database named ${DATABASE}."
    exit 2
else
    res=$(get_query_result "select 1 from information_schema.tables where table_name='schema_version';" "${DATABASE}")
    if [[ ${res} -eq  1 ]]; then
        echo "Database ${DATABASE} is not empty, please create an empty database named ${DATABASE}."
        exit 3
    fi
fi

echo "Restore of database ${DATABASE} from ${FILE} started..."
if file "${FILE}" | grep -q 'tar'; then
	# Creating the plpgsql language
	createlang --host="${SERVERNAME}" --port="${PORT}" --dbname="${DATABASE}" --username="${USERNAME}" plpgsql >& /dev/null
	restore_from_tar
	restore_from_tar_res=$?
else
	sed -i -e '/^CREATE DATABASE /d' -e '/ALTER DATABASE /d' "${FILE}"
	psql -w -h "${SERVERNAME}" -p "${PORT}" -U "${USERNAME}" -f "${FILE}"
fi

if [[ "${restore_from_tar_res}" -eq 0 ]]; then
	echo "Restore of database ${DATABASE} from ${FILE} completed."
	if [[ -z "${OMIT_UPGRADE}" ]]; then
		echo "Upgrading restored database..."
		./upgrade.sh -s "${SERVERNAME}" -p "${PORT}" -d "${DATABASE}" -u "${USERNAME}" -c
	fi
else
        popd > /dev/null
	exit 4
fi

fn_db_set_dbobjects_ownership
if [[ $? -ne 0 ]]; then
	echo "An error occurred while changing the ownership of objects in the database."
        popd > /dev/null
	exit 5
fi
popd > /dev/null
