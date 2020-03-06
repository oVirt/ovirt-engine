#!/bin/sh

. "$(dirname "$(dirname "$(dirname "$(readlink -f "$0")")")")"/bin/engine-prolog.sh
. "$(dirname "$(dirname "$(dirname "$(readlink -f "$0")")")")"/bin/generate-pgpass.sh
. "$(dirname "$0")/dbfunc-base.sh"

cleanup() {
	dbfunc_cleanup
	pgPassCleanup
}
trap cleanup 0

usage() {
	cat << __EOF__
Usage: $0 [options] [ENTITIES]

    -h            - This help text.
    -v            - Turn on verbosity                         (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output          (def. ${DBFUNC_LOGFILE})
    -t TYPE       - The object type {all | vm | template | disk | snapshot | illegal_images}
                    If "all" is used then no ENTITIES are expected.
    -r            - Recursive, unlocks all disks under the selected vm/template.
    -q            - Query db and display a list of the locked entites.
    -c            - Show column names.
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
	elif [ "${object_type}" = "illegal_images" ]; then
		CMD="UPDATE images SET imagestatus = '1' WHERE image_guid = '${id}';"
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

query_vm() {
	echo ""
	echo "Locked VMs"
	echo ""
	dbfunc_psql_die --command="
			select
				vm_name as vm_name
			from
				vm_static a,
				vm_dynamic b
			where
				a.vm_guid = b.vm_guid and
				status = ${IMAGE_LOCKED};

			select distinct
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
				is_plugged;

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
}

query_template() {
	echo ""
	echo "Locked templates"
	echo ""
	dbfunc_psql_die --command="
			select vm_name as template_name
			from vm_static
			where template_status = ${TEMPLATE_LOCKED};

			select distinct
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
				is_plugged;
		"
}

query_disk() {
	echo ""
	echo "Locked disks"
	echo ""
	dbfunc_psql_die --command="
			select distinct
				vm_id,
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
}

query_snapshot() {
	echo ""
	echo "Locked snapshots"
	echo ""
	dbfunc_psql_die --command="
			select
				vm_id,
				snapshot_id
			from
				snapshots a
			where
				status ilike '${SNAPSHOT_LOCKED}';
		"
}

query_illegal_images() {
	echo ""
	echo "Illegal images"
	echo ""
	dbfunc_psql_die --command="
			SELECT
				vm_name,
				image_guid
			FROM
				images a,
				vm_static b,
				vm_device c
			WHERE
				a.image_group_id = c.device_id AND
				b.vm_guid = c.vm_id AND
				imagestatus = '${ILLEGAL}';
		"
}

#Displays locked entities
entity_query() {
	local object_type="$1"
	local LOCKED=2
	local ILLEGAL=4
	local TEMPLATE_LOCKED=1
	local IMAGE_LOCKED=15;
	local SNAPSHOT_LOCKED=LOCKED

	if [ "${object_type}" = "vm" ]; then
		query_vm
	elif [ "${object_type}" = "template" ]; then
		query_template
	elif [ "${object_type}" = "disk" ]; then
		query_disk
	elif [ "${object_type}" = "snapshot" ]; then
		query_snapshot
	elif [ "${object_type}" = "illegal_images" ]; then
		query_illegal_images
	elif [ "${object_type}" = "all" ]; then
		query_vm
		query_template
		query_disk
		query_snapshot
		query_illegal_images
	fi
}

TYPE=
RECURSIVE=
QUERY=
IMPLICIT=
FORCE_ACTION=

DBFUNC_DB_HOST="${ENGINE_DB_HOST}"
DBFUNC_DB_PORT="${ENGINE_DB_PORT}"
DBFUNC_DB_USER="${ENGINE_DB_USER}"
DBFUNC_DB_DATABASE="${ENGINE_DB_DATABASE}"

while getopts hvl:t:rqicf option; do
	case $option in
		\?) usage; exit 1;;
		h) usage; exit 0;;
		v) DBFUNC_VERBOSE=1;;
		l) DBFUNC_LOGFILE="${OPTARG}";;
		t) TYPE="${OPTARG}";;
		r) RECURSIVE=1;;
		q) QUERY=1;;
		i) IMPLICIT=1;;
		c) DBFUNC_SHOW_HEADERS=1;;
		f) FORCE_ACTION=1;;
	esac
done

shift $(( $OPTIND - 1 ))
IDS="$@"

[ -n "${TYPE}" ] || die "Please specify type"
if [ "${TYPE}" != "all" ]; then
    [ -z "${IDS}" -a -z "${QUERY}" ] && die "Please specify ids or query"
    [ -n "${IDS}" -a -n "${QUERY}" ] && die "Please specify one ids or query"
fi

generatePgPass
DBFUNC_DB_PGPASSFILE="${MYPGPASS}"
dbfunc_init

# Install fn_db_unlock_all procedure
dbfunc_psql_die --file="$(dirname "$0")/unlock_entity.sql" > /dev/null

print_caution_msg() {
	echo ""
	echo "##########################################"
	echo "CAUTION, this operation may lead to data corruption and should be used with care. Please contact support prior to running this command"
	echo "##########################################"
	echo ""
	echo "Are you sure you want to proceed? [y/n]"
        if [ -n "${FORCE_ACTION}" ]; then
		answer="y"
	else
		read answer
	fi
	[ "${answer}" = "y" ] || die "Please contact support for further assistance."
}
# Execute
if [ -n "${QUERY}" ]; then
	entity_query "${TYPE}"
else
        if [ "${TYPE}" = "all" ]; then
	    print_caution_msg
            entity_unlock "${TYPE}" "" "$(whoami)" ${RECURSIVE}
        else
	    print_caution_msg

	    for ID in ${IDS} ; do
	    	entity_unlock "${TYPE}" "${ID}" "$(whoami)" ${RECURSIVE}
	    done
        fi
fi

# Drop fn_db_unlock_all procedure
dbfunc_psql_die --file="$(dirname "$0")/unlock_entity_drop.sql" > /dev/null

