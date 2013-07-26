package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.asynctasks.EndedTaskInfo;
import org.ovirt.engine.core.common.asynctasks.EndedTasksInfo;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class CommandMultiAsyncTasks {

    private java.util.HashMap<Guid, CommandAsyncTask> _listTasks;
    private Guid commandId;

    public Guid getCommandId() {
        return commandId;
    }

    private void setCommandId(Guid value) {
        commandId = value;
    }

    public CommandMultiAsyncTasks(Guid commandId) {
        _listTasks = new java.util.HashMap<Guid, CommandAsyncTask>();
        setCommandId(commandId);
    }

    public void AttachTask(CommandAsyncTask asyncTask) {
        synchronized (_listTasks) {
            if (!_listTasks.containsKey(asyncTask.getVdsmTaskId())) {
                log.infoFormat("CommandMultiAsyncTasks::AttachTask: Attaching task '{0}' to command '{1}'.",
                        asyncTask.getVdsmTaskId(), getCommandId());

                _listTasks.put(asyncTask.getVdsmTaskId(), asyncTask);
            }
        }
    }

    private java.util.ArrayList<CommandAsyncTask> getCurrentTasks() {
        java.util.ArrayList<CommandAsyncTask> retValue = new java.util.ArrayList<CommandAsyncTask>();

        for (CommandAsyncTask task : _listTasks.values()) {
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
            java.util.ArrayList<CommandAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (CommandAsyncTask task : CurrentActionTypeTasks) {
                // Check this is a task that is run on VDSM and is not in ENDED state.
                if (task.getState() != AsyncTaskState.Ended && !Guid.isNullOrEmpty(task.getVdsmTaskId())) {
                    log.infoFormat("Task with DB Task ID '{0}' and VDSM Task ID '{1}' is in state {2}. End action for command {3} will proceed when all the entity's tasks are completed.",
                            task.getParameters().getDbAsyncTask().getTaskId(),
                            task.getVdsmTaskId(),
                            task.getState(),
                            getCommandId());
                    return false;
                } else if (Guid.isNullOrEmpty(task.getVdsmTaskId())) {
                    log.infoFormat("task with DB task ID '{0}' has  empty vdsm  task ID and is about to be cleared",
                            task.getVdsmTaskId());
                }

            }
        }

        return true;
    }

    public void MarkAllWithAttemptingEndAction() {
        synchronized (_listTasks) {
            java.util.ArrayList<CommandAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (CommandAsyncTask task : CurrentActionTypeTasks) {
                task.setState(AsyncTaskState.AttemptingEndAction);
            }
        }
    }

    public EndedTasksInfo getEndedTasksInfo() {
        EndedTasksInfo endedTasksInfo = new EndedTasksInfo();
        java.util.ArrayList<EndedTaskInfo> endedTaskInfoList = new java.util.ArrayList<EndedTaskInfo>();

        synchronized (_listTasks) {
            java.util.ArrayList<CommandAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (CommandAsyncTask task : CurrentActionTypeTasks) {
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
            java.util.ArrayList<CommandAsyncTask> CurrentActionTypeTasks = getCurrentTasks();
            returnValue = CurrentActionTypeTasks.size();
        }

        return returnValue;
    }

    public void Repoll() {
        synchronized (_listTasks) {
            java.util.ArrayList<CommandAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (CommandAsyncTask task : CurrentActionTypeTasks) {
                task.setState(AsyncTaskState.Ended);
            }
        }
    }

    public void ClearTasks() {
        synchronized (_listTasks) {
            java.util.ArrayList<CommandAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (CommandAsyncTask task : CurrentActionTypeTasks) {
                if (task.getLastTaskStatus().getStatus() == AsyncTaskStatusEnum.finished) {
                    task.clearAsyncTask();
                }
            }
        }
    }

    public void StartPollingTask(Guid TaskID) {
        synchronized (_listTasks) {
            if (_listTasks.containsKey(TaskID) && _listTasks.get(TaskID).getParameters() != null
                    && _listTasks.get(TaskID).getParameters().getDbAsyncTask() != null) {
                for (CommandAsyncTask task : _listTasks.values()) {
                    if (task.getParameters() != null && task.getParameters().getDbAsyncTask() != null) {
                        task.setState(AsyncTaskState.Polling);
                    }
                }
            } else {
                log.warnFormat(
                        "CommandMultiAsyncTasks::StartPollingTask: For some reason, task '{0}' has no parameters or no DB task - we don't know its action type",
                        TaskID);
            }
        }
    }

    public boolean getAllCleared() {
        synchronized (_listTasks) {
            for (CommandAsyncTask task : _listTasks.values()) {
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
    private boolean taskWasCleared(CommandAsyncTask task) {
        AsyncTaskState taskState = task.getState();
        return taskState == AsyncTaskState.Cleared || taskState == AsyncTaskState.ClearFailed;
    }

    private static Log log = LogFactory.getLog(CommandMultiAsyncTasks.class);
}
