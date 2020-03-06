#!/bin/sh
###############################################################################################################
# The purpose of this utility is to display and clean asynchronous tasks or commands and corresponding
# Job steps/Compensation data.
# The utility enables to
# Display
#     All async tasks
#     Only Zombie tasks
#     All commands
#     All commands with tasks
#     Only Commands with zombie tasks
# Delete
#     All tasks
#     All Zombie tasks
#     A task related to a given task id
#     A Zombie task related to a given task id
#     A Command with zombie tasks given a command Id
#     All tasks related to a given command id
#     All Zombie tasks related to a given command id
#     All Commands with zombie tasks
#  Flags may be added (-C, -J) to specify if Job Steps & Compensation data
#  should be cleaned as well.
###############################################################################################################

. "$(dirname "$(dirname "$(dirname "$(readlink -f "$0")")")")"/bin/engine-prolog.sh
. "$(dirname "$(dirname "$(dirname "$(readlink -f "$0")")")")"/bin/generate-pgpass.sh
. "$(dirname "$0")/dbfunc-base.sh"

cleanup() {
	dbfunc_cleanup
	pgPassCleanup
}
trap cleanup 0

#Using two variables for sql commands in order to control command priority where data should be removed first from
#business_entity_snapshot and step table before removing it from the async_tasks table.
CMD1="";
CMD2="";
TASK_ID=""
COMMAND_ID=""
ZOMBIES_ONLY=
ZOMBIE_COMMANDS_ONLY=
COMMANDS_WITH_RUNNING_TASKS_ONLY=
ALL_COMMANDS=
CLEAR_ALL=
CLEAR_COMMANDS=
CLEAR_COMPENSATION=
CLEAR_JOB_STEPS=
CLEAR_JOB_STEPS_AND_COMPENSATION=
QUITE_MODE=
TASKS_FIELDS="task_id,task_type,status,started_at,result,action_type as command_type,command_id,step_id,storage_pool_id as DC"
COMMANDS_FIELDS="command_id,command_type,root_command_id,command_parameters,command_params_class,created_at,status,return_value,return_value_class,executed"

usage() {
    cat << __EOF__
Usage: $0 [options]

    -h            - This help text.
    -v            - Turn on verbosity                         (WARNING: lots of output)
    -l LOGFILE    - The logfile for capturing output          (def. ${DBFUNC_LOGFILE})
    -t TASK_ID    - Removes a task by its Task ID.
    -c COMMAND_ID - Removes all tasks related to the given Command Id.
    -T            - Removes/Displays all commands that have running tasks
    -o            - Removes/Displays all commands.
    -z            - Removes/Displays a Zombie task.
    -R            - Removes all tasks (use with -z to clear only zombie tasks).
    -r            - Removes all commands (use with -T to clear only those with running tasks. Use with -Z to clear only commands with zombie tasks.
    -Z            - Removes/Displays a command with zombie tasks.
    -C            - Clear related compensation entries.
    -J            - Clear related Job Steps.
    -A            - Clear all Job Steps and compensation entries.
    -q            - Quite mode, do not prompt for confirmation.

__EOF__
}

while getopts hvl:t:c:zZrRCJAToq option; do
	case "${option}" in
		\?) usage; exit 1;;
		h) usage; exit 0;;
		v) DBFUNC_VERBOSE=1;;
		l) DBFUNC_LOGFILE="${OPTARG}";;
		t) TASK_ID="${OPTARG}";;
		c) COMMAND_ID="${OPTARG}";;
		z) ZOMBIES_ONLY=1;;
		Z) ZOMBIE_COMMANDS_ONLY=1;;
		T) COMMANDS_WITH_RUNNING_TASKS_ONLY=1;;
		o) ALL_COMMANDS=1;;
		R) CLEAR_ALL=1;;
		r) CLEAR_COMMANDS=1;;
		C) CLEAR_COMPENSATION=1;;
		J) CLEAR_JOB_STEPS=1;;
		A) CLEAR_JOB_STEPS_AND_COMPENSATION=1;;
		q) QUITE_MODE=1;;
	esac
done

caution() {
	if [ -z "${QUITE_MODE}" ]; then
		# Highlight the expected results of selected operation.
		cat << __EOF__
$(tput smso) $1 $(tput rmso)
Caution, this operation should be used with care. Please contact support prior to running this command
Are you sure you want to proceed? [y/n]
__EOF__
		read answer
		[ "${answer}" = "y" ] || die "Please contact support for further assistance."
	fi
}

DBFUNC_DB_HOST="${ENGINE_DB_HOST}"
DBFUNC_DB_PORT="${ENGINE_DB_PORT}"
DBFUNC_DB_USER="${ENGINE_DB_USER}"
DBFUNC_DB_DATABASE="${ENGINE_DB_DATABASE}"

generatePgPass
DBFUNC_DB_PGPASSFILE="${MYPGPASS}"
dbfunc_init

dbfunc_psql_die --command="select exists (select * from information_schema.tables where table_schema = 'public' and table_name = 'command_entities');" | grep "t"

if [ "${TASK_ID}" != "" -o "${COMMAND_ID}" != "" -o -n "${CLEAR_ALL}" -o -n "${CLEAR_COMPENSATION}" -o -n "${CLEAR_JOB_STEPS}" ]; then #delete operations block
	if [ -n "${TASK_ID}" ]; then
		if [ -n "${ZOMBIES_ONLY}" ]; then
			CMD2="SELECT DeleteAsyncTaskZombiesByTaskId('${TASK_ID}');"
			if [ -n "${CLEAR_JOB_STEPS}" ]; then
				CMD1="SELECT DeleteJobStepsByTaskId('${TASK_ID}');"
				if [ -n "${CLEAR_COMPENSATION}" ]; then
					caution "This will remove the given Zombie Task, its Job Steps and related Compensation data!!!"
					CMD1="${CMD1}SELECT DeleteEntitySnapshotByZombieTaskId('${TASK_ID}');"
				else
					caution "This will remove the given Zombie Task and its related Job Steps!!!"
				fi
			else
				if [ -n "${CLEAR_COMPENSATION}" ]; then
					caution "This will remove the given Zombie Task and related Compensation data!!!"
					CMD1="${CMD1}SELECT DeleteEntitySnapshotByZombieTaskId('${TASK_ID}');"
				else
					caution "This will remove the given Zombie Task!!!"
				fi
			fi
		else
			CMD2="SELECT Deleteasync_tasks('${TASK_ID}');"
			if [ -n "${CLEAR_JOB_STEPS}" ]; then
				CMD1="SELECT DeleteJobStepsByTaskId('${TASK_ID}');"
				if [ -n "${CLEAR_COMPENSATION}" ]; then
					caution "This will remove the given Task its Job Steps and related Compensation data!!!"
					CMD1="${CMD1}SELECT DeleteEntitySnapshotByTaskId('${TASK_ID}');"
				else
					caution "This will remove the given Task and its related Job Steps!!!"
				fi
			else
				if [ -n "${CLEAR_COMPENSATION}" ]; then
					caution "This will remove the given Task and its related Compensation data!!!"
					CMD1="${CMD1}SELECT DeleteEntitySnapshotByTaskId('${TASK_ID}');"
				else
					caution "This will remove the given Task!!!"
				fi
			fi
		fi
	elif [ "${COMMAND_ID}" != "" ]; then
		if [ -n "${ZOMBIES_ONLY}" ]; then
			CMD2="SELECT DeleteAsyncTaskZombiesByCommandId('${COMMAND_ID}');"
			if [ -n "${CLEAR_COMPENSATION}" ]; then
				CMD1="SELECT delete_entity_snapshot_by_command_id('${COMMAND_ID}');"
				if [ -n "${CLEAR_JOB_STEPS}" ]; then
					caution "This will remove all Zombie Tasks of the given Command its Job Steps and its related Compensation data!!!"
					CMD1="${CMD1}SELECT DeleteJobStepsByZombieCommandId('${COMMAND_ID}');"
				else
					caution "This will remove all Zombie Tasks of the given Command and its related Compensation data!!!"
				fi
			else
				if [ -n "${CLEAR_JOB_STEPS}" ]; then
					caution "This will remove all Zombie Tasks of the given Command and its Job Steps!!!"
					CMD1="${CMD1}SELECT DeleteJobStepsByZombieCommandId('${COMMAND_ID}');"
				else
					caution "This will remove all Zombie Tasks of the given Command!!!"
				fi
			fi
		else
			CMD2="SELECT DeleteAsyncTaskByCommandId('${COMMAND_ID}');"
			if [ -n "${CLEAR_COMPENSATION}" ]; then
				CMD1="SELECT delete_entity_snapshot_by_command_id('${COMMAND_ID}');"
				if [ -n "${CLEAR_JOB_STEPS}" ]; then
					caution "This will remove all Tasks of the given Command its Job Steps and its related Compensation data!!!"
					CMD1="${CMD1}SELECT DeleteJobStepsByCommandId('${COMMAND_ID}');"
				else
					caution "This will remove all Tasks of the given Command and its related Compensation data!!!"
				fi
			else
				if [ -n "${CLEAR_JOB_STEPS}" ]; then
					caution "This will remove all Tasks of the given Command and its Job Steps!!!"
					CMD1="${CMD1}SELECT DeleteJobStepsByCommandId('${COMMAND_ID}');"
				else
					caution "This will remove all Tasks of the given Command!!!"
				fi
			fi
		fi
	elif [ -n "${CLEAR_ALL}" ]; then
		if [ -n "${ZOMBIES_ONLY}" ]; then
			CMD2="SELECT DeleteAsyncTasksZombies();"
			if [ -n "${CLEAR_JOB_STEPS_AND_COMPENSATION}" ]; then
				caution "This will remove all Zombie Tasks in async_tasks table, and all Job Steps and Compensation data!!!"
				CMD1="SELECT DeleteAllJobs(); SELECT DeleteAllEntitySnapshot();"
			else
				if [ -n "${CLEAR_COMPENSATION}" ]; then
					CMD1="${CMD1}SELECT DeleteEntitySnapshotZombies();"
					if [ -n "${CLEAR_JOB_STEPS}" ]; then
						caution "This will remove all Zombie Tasks in async_tasks table, its related Job Steps and Compensation data!!!"
						CMD1="${CMD1}SELECT DeleteJobStepsZombies();"
					else
						caution "This will remove all Zombie Tasks in async_tasks table and its related Compensation data!!!"
					fi
				else
					if [ -n "${CLEAR_JOB_STEPS}" ]; then
						caution "This will remove all Zombie Tasks in async_tasks table and its related Job Steps!!!"
						CMD1="${CMD1}SELECT DeleteJobStepsZombies();"
					else
						caution "This will remove all Zombie Tasks in async_tasks table!!!"
					fi
				fi
			fi
		else
			CMD2="TRUNCATE TABLE async_tasks cascade;"
			if [ -n "${CLEAR_JOB_STEPS_AND_COMPENSATION}" ]; then
				caution "This will remove all Tasks in async_tasks table, and all Job Steps and Compensation data!!!"
				CMD1="SELECT DeleteAllJobs(); SELECT DeleteAllEntitySnapshot();"
			else
				if [ -n "${CLEAR_COMPENSATION}" ]; then
					CMD1="TRUNCATE TABLE business_entity_snapshot cascade;"
					if [ -n "${CLEAR_JOB_STEPS}" ]; then
						caution "This will remove all Tasks in async_tasks table, its related Job Steps and Compensation data!!!"
						CMD1="${CMD1}TRUNCATE TABLE step cascade;TRUNCATE TABLE job CASCADE;"
					else
						caution "This will remove all async_tasks table content and its related Compensation data!!!"
					fi
				else
					if [ -n "${CLEAR_JOB_STEPS}" ]; then
						caution "This will remove all Tasks in async_tasks table and its related Job Steps!!!"
						CMD1="${CMD1}TRUNCATE TABLE step cascade;TRUNCATE TABLE job CASCADE;"
					else
						caution "This will remove all async_tasks table content!!!"
					fi
				fi
			fi
		fi
	else
		die "Please specify task"
	fi
elif [ -n "${ZOMBIES_ONLY}" ]; then #only display operations block
	CMD1="SELECT ${TASKS_FIELDS} FROM GetAsyncTasksZombies();"
elif [ -n "${ALL_COMMANDS}" ]; then #only display commands
	CMD1="SELECT ${COMMANDS_FIELDS} FROM GetAllCommands();"
elif [ -n "${COMMANDS_WITH_RUNNING_TASKS_ONLY}" ]; then
	CMD1="SELECT ${COMMANDS_FIELDS} FROM GetAllCommandsWithRunningTasks();"
elif [ -n "${CLEAR_COMMANDS}" ]; then
	if [ -n "${COMMANDS_WITH_RUNNING_TASKS_ONLY}" ]; then
		CMD1="SELECT DeleteAllCommandsWithRunningTasks();"
	elif [ -n "${ZOMBIE_COMMANDS_ONLY}" ]; then
		CMD1="SELECT DeleteAllCommandsWithZombieTasks();"
	else
		CMD1="SELECT DeleteAllCommands();"
	fi
elif [ -n "${ZOMBIE_COMMANDS_ONLY}" ]; then
	CMD1="SELECT ${COMMANDS_FIELDS} FROM GetAllCommandsWithZombieTasks();"
else
	CMD1="SELECT ${TASKS_FIELDS} FROM GetAllFromasync_tasks();"
fi

# Install taskcleaner procedures
dbfunc_psql_die --file="$(dirname "$0")/taskcleaner_sp.sql" > /dev/null
dbfunc_psql_die --file="$(dirname "$0")/taskcleaner_sp_3_5.sql" > /dev/null

# Execute
dbfunc_psql_die --command="${CMD1}${CMD2}"

# Drop taskcleaner procedures
dbfunc_psql_die --file="$(dirname "$0")/taskcleaner_sp_drop.sql" > /dev/null

