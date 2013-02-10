package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.EndedTaskInfo;
import org.ovirt.engine.core.common.asynctasks.EndedTasksInfo;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class EntityMultiAsyncTasks {
    private VdcActionType privateActionType = VdcActionType.forValue(0);

    public VdcActionType getActionType() {
        return privateActionType;
    }

    public void setActionType(VdcActionType value) {
        privateActionType = value;
    }

    private java.util.HashMap<Guid, EntityAsyncTask> _listTasks;
    private Object privateContainerId;

    public Object getContainerId() {
        return privateContainerId;
    }

    private void setContainerId(Object value) {
        privateContainerId = value;
    }

    public EntityMultiAsyncTasks(Object containerID) {
        _listTasks = new java.util.HashMap<Guid, EntityAsyncTask>();
        setContainerId(containerID);
    }

    public void AttachTask(EntityAsyncTask asyncTask) {
        synchronized (_listTasks) {
            if (!_listTasks.containsKey(asyncTask.getTaskID())) {
                log.infoFormat("EntityMultiAsyncTasks::AttachTask: Attaching task '{0}' to entity '{1}'.",
                        asyncTask.getTaskID(), getContainerId());

                _listTasks.put(asyncTask.getTaskID(), asyncTask);
            }
        }
    }

    private java.util.ArrayList<EntityAsyncTask> GetCurrentActionTypeTasks() {
        java.util.ArrayList<EntityAsyncTask> retValue = new java.util.ArrayList<EntityAsyncTask>();

        for (EntityAsyncTask task : _listTasks.values()) {
            if (task.getParameters() != null
                    && task.getParameters().getDbAsyncTask() != null
                    && task.getParameters().getDbAsyncTask().getaction_type() == getActionType()
                    && (task.getState() == AsyncTaskState.Polling || task.getState() == AsyncTaskState.Ended || task
                            .getState() == AsyncTaskState.AttemptingEndAction)) {
                retValue.add(task);
            }
        }

        return retValue;
    }

    public boolean ShouldEndAction() {
        synchronized (_listTasks) {
            java.util.ArrayList<EntityAsyncTask> CurrentActionTypeTasks = GetCurrentActionTypeTasks();

            for (EntityAsyncTask task : CurrentActionTypeTasks) {
                if (task.getState() != AsyncTaskState.Ended) {
                    return false;
                }
            }
        }

        return true;
    }

    public void MarkAllWithAttemptingEndAction() {
        synchronized (_listTasks) {
            java.util.ArrayList<EntityAsyncTask> CurrentActionTypeTasks = GetCurrentActionTypeTasks();

            for (EntityAsyncTask task : CurrentActionTypeTasks) {
                task.setState(AsyncTaskState.AttemptingEndAction);
            }
        }
    }

    public EndedTasksInfo getEndedTasksInfo() {
        EndedTasksInfo endedTasksInfo = new EndedTasksInfo();
        java.util.ArrayList<EndedTaskInfo> endedTaskInfoList = new java.util.ArrayList<EndedTaskInfo>();

        synchronized (_listTasks) {
            java.util.ArrayList<EntityAsyncTask> CurrentActionTypeTasks = GetCurrentActionTypeTasks();

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
            java.util.ArrayList<EntityAsyncTask> CurrentActionTypeTasks = GetCurrentActionTypeTasks();
            returnValue = CurrentActionTypeTasks.size();
        }

        return returnValue;
    }

    public void Repoll() {
        synchronized (_listTasks) {
            java.util.ArrayList<EntityAsyncTask> CurrentActionTypeTasks = GetCurrentActionTypeTasks();

            for (EntityAsyncTask task : CurrentActionTypeTasks) {
                task.setState(AsyncTaskState.Ended);
            }
        }
    }

    /**
     * Reset the action type if all the current action type's tasks are cleared, so that if there are any other tasks
     * in the list they will get treated correctly.
     */
    protected void resetActionTypeIfNecessary() {
        boolean allCleared = true;
        synchronized (_listTasks) {
            List<EntityAsyncTask> CurrentActionTypeTasks = GetCurrentActionTypeTasks();

            for (EntityAsyncTask task : CurrentActionTypeTasks) {
                allCleared = allCleared && taskWasCleared(task);
            }

            if (allCleared) {
                setActionType(VdcActionType.Unknown);
            }
        }
    }

    public void ClearTasks() {
        synchronized (_listTasks) {
            java.util.ArrayList<EntityAsyncTask> CurrentActionTypeTasks = GetCurrentActionTypeTasks();

            for (EntityAsyncTask task : CurrentActionTypeTasks) {
                task.clearAsyncTask();
            }

            StartPollingNextTask();
        }
    }

    // call this method after ending action and clearing tasks of current
    // ActionType.
    protected void StartPollingNextTask() {
        synchronized (_listTasks) {
            for (EntityAsyncTask task : _listTasks.values()) {
                if (task.getState() == AsyncTaskState.WaitForPoll && task.getParameters() != null
                        && task.getParameters().getDbAsyncTask() != null) {
                    log.infoFormat(
                            "EntityMultiAsyncTasks::StartPollingNextTask: Starting to poll next task " +
                            "(task ID: '{0}', action type '{1}')",
                            task.getTaskID(),
                            task.getParameters().getDbAsyncTask().getaction_type());

                    setActionType(VdcActionType.Unknown);
                    StartPollingTask(task.getTaskID());
                    break;
                }
            }
        }
    }

    public void StartPollingTask(Guid TaskID) {
        synchronized (_listTasks) {
            if (_listTasks.containsKey(TaskID) && _listTasks.get(TaskID).getParameters() != null
                    && _listTasks.get(TaskID).getParameters().getDbAsyncTask() != null) {
                if (getActionType() == VdcActionType.Unknown) {
                    // still no ActionType chosen -> determine the ActionType by
                    // the
                    // TaskID that we want to start polling and start polling
                    // all of
                    // its siblings:
                    setActionType(_listTasks.get(TaskID).getParameters().getDbAsyncTask().getaction_type());
                    log.infoFormat(
                            "EntityMultiAsyncTasks::StartPollingTask: Current Action Type for entity '{0}' is '{1}' (determined by task '{2}')",
                            getContainerId(),
                            getActionType(),
                            TaskID);

                    for (EntityAsyncTask task : _listTasks.values()) {
                        if (task.getParameters() != null
                                && task.getParameters().getDbAsyncTask() != null
                                && task.getParameters().getDbAsyncTask().getaction_type() == getActionType()
                                && (task.getState() == AsyncTaskState.Initializing || task.getState() == AsyncTaskState.WaitForPoll)) {
                            task.setState(AsyncTaskState.Polling);
                        }
                    }
                }

                else {
                    // ActionType is already determined -> set the task and its
                    // siblings
                    // to 'wait for poll':
                    VdcActionType currTaskActionType = _listTasks.get(TaskID).getParameters().getDbAsyncTask()
                            .getaction_type();
                    for (EntityAsyncTask task : _listTasks.values()) {
                        if (task.getParameters() != null && task.getParameters().getDbAsyncTask() != null
                                && task.getParameters().getDbAsyncTask().getaction_type() == currTaskActionType
                                && task.getState() == AsyncTaskState.Initializing) {
                            task.setState(AsyncTaskState.WaitForPoll);
                        }
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
