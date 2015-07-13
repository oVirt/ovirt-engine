#!/bin/sh

. "$(dirname "$0")/dbfunc-base.sh"

cleanup() {
	dbfunc_cleanup
}
trap cleanup 0
dbfunc_init

usage() {
	cat << __EOF__
Usage: $0 [options] [ENTITIES]

    -h            - This help text.
    -v            - Turn on verbosity                         (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output          (def. ${DBFUNC_LOGFILE})
    -s HOST       - The database servername for the database  (def. ${DBFUNC_DB_HOST})
    -p PORT       - The database port for the database        (def. ${DBFUNC_DB_PORT})
    -u USER       - The username for the database             (def. ${DBFUNC_DB_USER})
    -d DATABASE   - The database name                         (def. ${DBFUNC_DB_DATABASE})
    -t TYPE       - The object type {all | vm | template | disk | snapshot}
                    If "all" is used then no ENTITIES are expected.
    -r            - Recursive, unlocks all disks under the selected vm/template.
    -q            - Query db and display a list of the locked entites.
    ENTITIES      - The list of object names in case of vm/template, UUIDs in case of a disk

    NOTE: This utility access the database and should have the
          corresponding credentals.

          In case that a password is used to access the database PGPASSWORD
          or PGPASSFILE should be set.

    Example:
        \$ PGPASSWORD=xxxxxx ./unlock_entity.sh -t disk -q

__EOF__
}

#unlocks the given VM/Template and its disks or a given disk
#in case of VM/Template the id is the name, in case of a disk, the id is the disk UUID
entity_unlock() {
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
	elif [ "${object_type}" = "all" ]; then
		CMD="select fn_db_unlock_all();"
	else
		printf "Error: $* "
	fi

	if [ -n "${CMD}" ]; then
		echo "${CMD}"
		if dbfunc_psql --command="${CMD}"; then
                	# Generate audit log entry only if not called implicitly from other program(engine-setup for example)
                	if [ ! -n "${IMPLICIT}" ]; then
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
						'${0} :  System user ${user} run manually unlock_entity script on entity [type,id] [${object_type},${id}] with db user ${DBFUNC_DB_USER}'
					)
				"
                        fi
			echo "unlock ${object_type} ${id} completed successfully."
		else
			echo "unlock ${object_type} ${id} completed with errors."
		fi
	fi
}

#Displays locked entities
entity_query() {
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

TYPE=
RECURSIVE=
QUERY=
IMPLICIT=

while getopts hvl:s:p:u:d:t:rqi option; do
	case $option in
		\?) usage; exit 1;;
		h) usage; exit 0;;
		v) DBFUNC_VERBOSE=1;;
		l) DBFUNC_LOGFILE="${OPTARG}";;
		s) DBFUNC_DB_HOST="${OPTARG}";;
		p) DBFUNC_DB_PORT="${OPTARG}";;
		u) DBFUNC_DB_USER="${OPTARG}";;
		d) DBFUNC_DB_DATABASE="${OPTARG}";;
		t) TYPE="${OPTARG}";;
		r) RECURSIVE=1;;
		q) QUERY=1;;
		i) IMPLICIT=1;;
	esac
done

shift $(( $OPTIND - 1 ))
IDS="$@"

[ -n "${TYPE}" ] || die "Please specify type"
if [ "${TYPE}" != "all" ]; then
    [ -z "${IDS}" -a -z "${QUERY}" ] && die "Please specify ids or query"
    [ -n "${IDS}" -a -n "${QUERY}" ] && die "Please specify one ids or query"
fi

# Install fn_db_unlock_all procedure
dbfunc_psql_die --file="$(dirname "$0")/unlock_entity.sql" > /dev/null

# Execute
if [ -n "${QUERY}" ]; then
	entity_query "${TYPE}"
else
        if [ "${TYPE}" = "all" ]; then
            entity_unlock "${TYPE}" "" "$(whoami)" ${RECURSIVE}
        else
	    echo "Caution, this operation may lead to data corruption and should be used with care. Please contact support prior to running this command"
	    echo "Are you sure you want to proceed? [y/n]"
	    read answer
	    [ "${answer}" = "y" ] || die "Please contact support for further assistance."

	    for ID in ${IDS} ; do
	    	entity_unlock "${TYPE}" "${ID}" "$(whoami)" ${RECURSIVE}
	    done
        fi
fi

# Drop fn_db_unlock_all procedure
dbfunc_psql_die --file="$(dirname "$0")/unlock_entity_drop.sql" > /dev/null

