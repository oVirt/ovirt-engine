#!/bin/bash
###############################################################################################################
# The purpose of this utility is to display and clean asynchronous tasks and corresponding
# Job steps/Compensation data.
# The utility enables to
# Display
#     All async tasks
#     Only Zombie tasks
# Delete
#     All tasks
#     All Zombie tasks
#     A task related to a given task id
#     A Zombie task related to a given task id
#     All tasks related to a given command id
#     All Zombie tasks related to a given command id
#  Flags may be added (-C, -J) to specify if Job Steps & Compensation data
#  should be cleaned as well.
###############################################################################################################

pushd $(dirname ${0})>/dev/null
source ./common.sh


#setting defaults
set_defaults

usage() {
    printf "Usage: ${ME} [-h] [-s server] [-p PORT]] [-d DATABASE] -u USERNAME [-l LOGFILE]  [-t taskId] [-c commandId] [-z] [-R] [-C] [-J] [-A] [-q] [-v]\n"
    printf "\n"
    printf "\t-s SERVERNAME - The database servername for the database  (def. ${SERVERNAME})\n"
    printf "\t-p PORT       - The database port for the database        (def. ${PORT})\n"
    printf "\t-d DATABASE   - The database name                         (def. ${DATABASE})\n"
    printf "\t-u USERNAME   - The username for the database             (def. engine)\n"
    printf "\t-l LOGFILE    - The logfile for capturing output          (def. ${LOGFILE})\n"
    printf "\t-t TASK_ID    - Removes a task by its Task ID.\n"
    printf "\t-c COMMAND_ID - Removes all tasks related to the given Command Id.\n"
    printf "\t-z            - Removes/Displays a Zombie task.\n"
    printf "\t-R            - Removes all Zombie tasks.\n"
    printf "\t-C            - Clear related compensation entries.\n"
    printf "\t-J            - Clear related Job Steps.\n"
    printf "\t-A            - Clear all Job Steps and compensation entries.\n"
    printf "\t-q            - Quite mode, do not prompt for confirmation.\n"
    printf "\t-v            - Turn on verbosity                         (WARNING: lots of output)\n"
    printf "\t-h            - This help text.\n"
    printf "\n"
    popd>/dev/null
    exit $ret
}

DEBUG () {
    if $VERBOSE; then
        printf "DEBUG: $*"
    fi
}

#Using two variables for sql commands in order to control command priority where data should be removed first from
#business_entity_snapshot and step table before removing it from the async_tasks table.
CMD1="";
CMD2="";
TASK_ID=""
COMMAND_ID=""
ZOMBIES_ONLY=false
CLEAR_ALL=false
CLEAR_COMPENSATION=false
CLEAR_JOB_STEPS=false
CLEAR_JOB_STEPS_AND_COMPENSATION=false
QUITE_MODE=false
FIELDS="task_id,task_type,status,started_at,result,action_type as command_type,command_id,step_id,storage_pool_id as DC"

while getopts hs:d:u:p:l:t:c:zRCJAqv option; do
    case $option in
        s) SERVERNAME=$OPTARG;;
        p) PORT=$OPTARG;;
        d) DATABASE=$OPTARG;;
        u) USERNAME=$OPTARG;;
        l) LOGFILE=$OPTARG;;
        t) TASK_ID=$OPTARG;;
        c) COMMAND_ID=$OPTARG;;
        z) ZOMBIES_ONLY=true;;
        R) CLEAR_ALL=true;;
        C) CLEAR_COMPENSATION=true;;
        J) CLEAR_JOB_STEPS=true;;
        A) CLEAR_JOB_STEPS_AND_COMPENSATION=true;;
        q) QUITE_MODE=true;;
        v) VERBOSE=true;;
        h) ret=0 && usage;;
       \?) ret=1 && usage;;
    esac
done

caution() {
    if [ "${QUITE_MODE}" = "false" ]; then
        # Highlight the expected results of selected operation.
        echo $(tput smso) $1 $(tput rmso)
        echo "Caution, this operation should be used with care. Please contact support prior to running this command"
        echo "Are you sure you want to proceed? [y/n]"
        read answer
        if [ "${answer}" = "n" ]; then
           echo "Please contact support for further assistance."
           popd>/dev/null
           exit 1
        fi
    fi
}

if [[ ! -n "${USERNAME}" ]]; then
   usage
   exit 1
fi


# Install taskcleaner procedures
psql -w -U ${USERNAME} -h ${SERVERNAME} -p ${PORT} -f ./taskcleaner_sp.sql ${DATABASE} > /dev/null
status=$?
if [ ${status} -ne 0 ]; then
    exit ${status}
fi

if [ "${TASK_ID}" != "" -o "${COMMAND_ID}" != "" -o  "${CLEAR_ALL}" = "true" -o "${CLEAR_COMPENSATION}" = "true" -o "${CLEAR_JOB_STEPS}" = "true" ]; then #delete operations block
    if [ "${TASK_ID}" != "" ]; then
        if [ "${ZOMBIES_ONLY}" = "true" ]; then
            CMD2="SELECT DeleteAsyncTaskZombiesByTaskId('${TASK_ID}');"
            if [ "${CLEAR_JOB_STEPS}" = "true" ]; then
                CMD1="SELECT DeleteJobStepsByTaskId('${TASK_ID}');"
                if [ "${CLEAR_COMPENSATION}" = "true" ]; then
                    caution "This will remove the given Zombie Task, its Job Steps and related Compensation data!!!"
                    CMD1="${CMD1}SELECT DeleteEntitySnapshotByZombieTaskId('${TASK_ID}');"
                else
                    caution "This will remove the given Zombie Task and its related Job Steps!!!"
                fi
            else
                if [ "${CLEAR_COMPENSATION}" = "true" ]; then
                    caution "This will remove the given Zombie Task and related Compensation data!!!"
                    CMD1="${CMD1}SELECT DeleteEntitySnapshotByZombieTaskId('${TASK_ID}');"
                else
                    caution "This will remove the given Zombie Task!!!"
                fi
            fi
        else
            CMD2="SELECT Deleteasync_tasks('${TASK_ID}');"
            if [ "${CLEAR_JOB_STEPS}" = "true" ]; then
                CMD1="SELECT DeleteJobStepsByTaskId('${TASK_ID}');"
                if [ "${CLEAR_COMPENSATION}" = "true" ]; then
                    caution "This will remove the given Task its Job Steps and related Compensation data!!!"
                    CMD1="${CMD1}SELECT DeleteEntitySnapshotByTaskId('${TASK_ID}');"
                else
                    caution "This will remove the given Task and its related Job Steps!!!"
                fi
            else
                if [ "${CLEAR_COMPENSATION}" = "true" ]; then
                    caution "This will remove the given Task and its related Compensation data!!!"
                    CMD1="${CMD1}SELECT DeleteEntitySnapshotByTaskId('${TASK_ID}');"
                else
                    caution "This will remove the given Task!!!"
                fi
            fi
        fi
    elif [ "${COMMAND_ID}" != "" ]; then
        if [ "${ZOMBIES_ONLY}" = "true" ]; then
            CMD2="SELECT DeleteAsyncTaskZombiesByCommandId('${COMMAND_ID}');"
            if [ "${CLEAR_COMPENSATION}" = "true" ]; then
                CMD1="SELECT delete_entity_snapshot_by_command_id('${COMMAND_ID}');"
                if [ "${CLEAR_JOB_STEPS}" = "true" ]; then
                    caution "This will remove all Zombie Tasks of the given Command its Job Steps and its related Compensation data!!!"
                    CMD1="${CMD1}SELECT DeleteJobStepsByZombieCommandId('${COMMAND_ID}');"
                else
                    caution "This will remove all Zombie Tasks of the given Command and its related Compensation data!!!"
                fi
            else
                if [ "${CLEAR_JOB_STEPS}" = "true" ]; then
                    caution "This will remove all Zombie Tasks of the given Command and its Job Steps!!!"
                    CMD1="${CMD1}SELECT DeleteJobStepsByZombieCommandId('${COMMAND_ID}');"
                else
                    caution "This will remove all Zombie Tasks of the given Command!!!"
                fi
            fi
        else
            CMD2="SELECT DeleteAsyncTaskByCommandId('${COMMAND_ID}');"
            if [ "${CLEAR_COMPENSATION}" = "true" ]; then
                CMD1="SELECT delete_entity_snapshot_by_command_id('${COMMAND_ID}');"
                if [ "${CLEAR_JOB_STEPS}" = "true" ]; then
                    caution "This will remove all Tasks of the given Command its Job Steps and its related Compensation data!!!"
                    CMD1="${CMD1}SELECT DeleteJobStepsByCommandId('${COMMAND_ID}');"
                else
                    caution "This will remove all Tasks of the given Command and its related Compensation data!!!"
                fi
            else
                if [ "${CLEAR_JOB_STEPS}" = "true" ]; then
                    caution "This will remove all Tasks of the given Command and its Job Steps!!!"
                    CMD1="${CMD1}SELECT DeleteJobStepsByCommandId('${COMMAND_ID}');"
                else
                    caution "This will remove all Tasks of the given Command!!!"
                fi
            fi
        fi
    elif [ "${CLEAR_ALL}" = "true" ]; then
        if [ "${ZOMBIES_ONLY}" = "true" ]; then
            CMD2="SELECT DeleteAsyncTasksZombies();"
            if [ "${CLEAR_JOB_STEPS_AND_COMPENSATION}" = "true" ]; then
                caution "This will remove all Zombie Tasks in async_tasks table, and all Job Steps and Compensation data!!!"
                CMD1="SELECT DeleteAllJobs(); SELECT DeleteAllEntitySnapshot();"
            else
                if [ "${CLEAR_COMPENSATION}" = "true" ]; then
                    CMD1="${CMD1}SELECT DeleteEntitySnapshotZombies();"
                    if [ "${CLEAR_JOB_STEPS}" = "true" ]; then
                        caution "This will remove all Zombie Tasks in async_tasks table, its related Job Steps and Compensation data!!!"
                        CMD1="${CMD1}SELECT DeleteJobStepsZombies();"
                    else
                        caution "This will remove all Zombie Tasks in async_tasks table and its related Compensation data!!!"
                    fi
                else
                    if [ "${CLEAR_JOB_STEPS}" = "true" ]; then
                        caution "This will remove all Zombie Tasks in async_tasks table and its related Job Steps!!!"
                        CMD1="${CMD1}SELECT DeleteJobStepsZombies();"
                    else
                        caution "This will remove all Zombie Tasks in async_tasks table!!!"
                    fi
                fi
            fi
        else
            CMD2="TRUNCATE TABLE async_tasks cascade;"
            if [ "${CLEAR_JOB_STEPS_AND_COMPENSATION}" = "true" ]; then
                caution "This will remove all Tasks in async_tasks table, and all Job Steps and Compensation data!!!"
                CMD1="SELECT DeleteAllJobs(); SELECT DeleteAllEntitySnapshot();"
            else
                if [ "${CLEAR_COMPENSATION}" = "true" ]; then
                    CMD1="TRUNCATE TABLE business_entity_snapshot cascade;"
                    if [ "${CLEAR_JOB_STEPS}" = "true" ]; then
                        caution "This will remove all Tasks in async_tasks table, its related Job Steps and Compensation data!!!"
                        CMD1="${CMD1}TRUNCATE TABLE step cascade;"
                    else
                        caution "This will remove all async_tasks table content and its related Compensation data!!!"
                    fi
                else
                    if [ "${CLEAR_JOB_STEPS}" = "true" ]; then
                        caution "This will remove all Tasks in async_tasks table and its related Job Steps!!!"
                        CMD1="${CMD1}TRUNCATE TABLE step cascade;"
                    else
                        caution "This will remove all async_tasks table content!!!"
                    fi
                fi
            fi
        fi
    else
        usage
    fi
elif [ "${ZOMBIES_ONLY}" = "true" ]; then #only display operations block

        CMD1="SELECT ${FIELDS} FROM GetAsyncTasksZombies();"
else
        CMD1="SELECT ${FIELDS} FROM GetAllFromasync_tasks();"
fi

psql -w -U ${USERNAME} -h ${SERVERNAME} -p ${PORT} -c "${CMD1}${CMD2}" -x ${DATABASE}
status=$?
popd>/dev/null
exit ${status}
