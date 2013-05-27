package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.asynctasks.EndedTaskInfo;
import org.ovirt.engine.core.common.asynctasks.EndedTasksInfo;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class EntityMultiAsyncTasks {

    private java.util.HashMap<Guid, EntityAsyncTask> _listTasks;
    private Guid commandId;

    public Guid getCommandId() {
        return commandId;
    }

    private void setCommandId(Guid value) {
        commandId = value;
    }

    public EntityMultiAsyncTasks(Guid commandId) {
        _listTasks = new java.util.HashMap<Guid, EntityAsyncTask>();
        setCommandId(commandId);
    }

    public void AttachTask(EntityAsyncTask asyncTask) {
        synchronized (_listTasks) {
            if (!_listTasks.containsKey(asyncTask.getVdsmTaskId())) {
                log.infoFormat("EntityMultiAsyncTasks::AttachTask: Attaching task '{0}' to command '{1}'.",
                        asyncTask.getVdsmTaskId(), getCommandId());

                _listTasks.put(asyncTask.getVdsmTaskId(), asyncTask);
            }
        }
    }

    private java.util.ArrayList<EntityAsyncTask> getCurrentTasks() {
        java.util.ArrayList<EntityAsyncTask> retValue = new java.util.ArrayList<EntityAsyncTask>();

        for (EntityAsyncTask task : _listTasks.values()) {
            if (task.getParameters() != null
                    && task.getParameters().getDbAsyncTask() != null
                    && (task.getState() == AsyncTaskState.Polling || task.getState() == AsyncTaskState.Ended || task
                            .getState() == AsyncTaskState.AttemptingEndAction)) {
                retValue.add(task);
            }
        }

        return retValue;
    }

    public boolean ShouldEndAction() {
        synchronized (_listTasks) {
            java.util.ArrayList<EntityAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (EntityAsyncTask task : CurrentActionTypeTasks) {
                if (task.getState() != AsyncTaskState.Ended) {
                    log.infoFormat("Task ID: '{0}' is in state {1}. End action for command {2} will proceed when all the entity's tasks are completed.",
                            task.getVdsmTaskId(),
                            task.getState(),
                            getCommandId());
                    return false;
                }
            }
        }

        return true;
    }

    public void MarkAllWithAttemptingEndAction() {
        synchronized (_listTasks) {
            java.util.ArrayList<EntityAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (EntityAsyncTask task : CurrentActionTypeTasks) {
                task.setState(AsyncTaskState.AttemptingEndAction);
            }
        }
    }

    public EndedTasksInfo getEndedTasksInfo() {
        EndedTasksInfo endedTasksInfo = new EndedTasksInfo();
        java.util.ArrayList<EndedTaskInfo> endedTaskInfoList = new java.util.ArrayList<EndedTaskInfo>();

        synchronized (_listTasks) {
            java.util.ArrayList<EntityAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (EntityAsyncTask task : CurrentActionTypeTasks) {
                task.setLastStatusAccessTime();
                EndedTaskInfo tempVar = new EndedTaskInfo();
                tempVar.setTaskStatus(task.getLastTaskStatus());
                tempVar.setTaskParameters(task.getParameters());
                endedTaskInfoList.add(tempVar);
            }

            endedTasksInfo.setTasksInfo(endedTaskInfoList);
        }

        return endedTasksInfo;
    }

    public int getTasksCountCurrentActionType() {
        int returnValue = 0;
        synchronized (_listTasks) {
            java.util.ArrayList<EntityAsyncTask> CurrentActionTypeTasks = getCurrentTasks();
            returnValue = CurrentActionTypeTasks.size();
        }

        return returnValue;
    }

    public void Repoll() {
        synchronized (_listTasks) {
            java.util.ArrayList<EntityAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (EntityAsyncTask task : CurrentActionTypeTasks) {
                task.setState(AsyncTaskState.Ended);
            }
        }
    }

    public void ClearTasks() {
        synchronized (_listTasks) {
            java.util.ArrayList<EntityAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (EntityAsyncTask task : CurrentActionTypeTasks) {
                task.clearAsyncTask();
            }
        }
    }

    public void StartPollingTask(Guid TaskID) {
        synchronized (_listTasks) {
            if (_listTasks.containsKey(TaskID) && _listTasks.get(TaskID).getParameters() != null
                    && _listTasks.get(TaskID).getParameters().getDbAsyncTask() != null) {
                for (EntityAsyncTask task : _listTasks.values()) {
                    if (task.getParameters() != null && task.getParameters().getDbAsyncTask() != null) {
                        task.setState(AsyncTaskState.Polling);
                    }
                }
            } else {
                log.warnFormat(
                        "EntityMultiAsyncTasks::StartPollingTask: For some reason, task '{0}' has no parameters or no DB task - we don't know its action type",
                        TaskID);
            }
        }
    }

    public boolean getAllCleared() {
        synchronized (_listTasks) {
            for (EntityAsyncTask task : _listTasks.values()) {
                if (!taskWasCleared(task)) {
                    log.infoFormat("[within thread]: Some of the tasks related to command id {0} were not cleared yet (Task id {1} is in state {2}).",
                            getCommandId(),
                            task.getVdsmTaskId(),
                            task.getState());
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * @param task
     *            The task to check.
     * @return Whether the task is cleared (succeeded or failed) or not cleared.
     */
    private boolean taskWasCleared(EntityAsyncTask task) {
        AsyncTaskState taskState = task.getState();
        return taskState == AsyncTaskState.Cleared || taskState == AsyncTaskState.ClearFailed;
    }

    private static Log log = LogFactory.getLog(EntityMultiAsyncTasks.class);
}
