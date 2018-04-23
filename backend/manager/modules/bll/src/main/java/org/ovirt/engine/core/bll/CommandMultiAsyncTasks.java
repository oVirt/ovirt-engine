package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.tasks.AsyncTaskState;
import org.ovirt.engine.core.bll.tasks.CommandAsyncTask;
import org.ovirt.engine.core.common.asynctasks.EndedTaskInfo;
import org.ovirt.engine.core.common.asynctasks.EndedTasksInfo;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandMultiAsyncTasks {
    private static final Logger log = LoggerFactory.getLogger(CommandMultiAsyncTasks.class);

    private Map<Guid, CommandAsyncTask> _listTasks;
    private Guid commandId;

    public Guid getCommandId() {
        return commandId;
    }

    private void setCommandId(Guid value) {
        commandId = value;
    }

    public CommandMultiAsyncTasks(Guid commandId) {
        _listTasks = new HashMap<>();
        setCommandId(commandId);
    }

    public void attachTask(CommandAsyncTask asyncTask) {
        synchronized (_listTasks) {
            if (!_listTasks.containsKey(asyncTask.getVdsmTaskId())) {
                log.info("CommandMultiAsyncTasks::attachTask: Attaching task '{}' to command '{}'.",
                        asyncTask.getVdsmTaskId(), getCommandId());

                _listTasks.put(asyncTask.getVdsmTaskId(), asyncTask);
            }
        }
    }

    private ArrayList<CommandAsyncTask> getCurrentTasks() {
        ArrayList<CommandAsyncTask> retValue = new ArrayList<>();

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

    public boolean shouldEndAction() {
        synchronized (_listTasks) {
            ArrayList<CommandAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (CommandAsyncTask task : CurrentActionTypeTasks) {
                // Check this is a task that is run on VDSM and is not in ENDED state.
                if (task.getState() != AsyncTaskState.Ended && !Guid.isNullOrEmpty(task.getVdsmTaskId())) {
                    log.info("Task with DB Task ID '{}' and VDSM Task ID '{}' is in state {}. End action for command"
                                    + " {} will proceed when all the entity's tasks are completed.",
                            task.getParameters().getDbAsyncTask().getTaskId(),
                            task.getVdsmTaskId(),
                            task.getState(),
                            getCommandId());
                    return false;
                } else if (Guid.isNullOrEmpty(task.getVdsmTaskId())) {
                    log.info("Task with DB task ID '{}' has  empty vdsm  task ID and is about to be cleared",
                            task.getVdsmTaskId());
                }

            }
        }

        return true;
    }

    public void markAllWithAttemptingEndAction() {
        synchronized (_listTasks) {
            ArrayList<CommandAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (CommandAsyncTask task : CurrentActionTypeTasks) {
                task.setState(AsyncTaskState.AttemptingEndAction);
            }
        }
    }

    public EndedTasksInfo getEndedTasksInfo() {
        EndedTasksInfo endedTasksInfo = new EndedTasksInfo();
        ArrayList<EndedTaskInfo> endedTaskInfoList = new ArrayList<>();

        synchronized (_listTasks) {
            ArrayList<CommandAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

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
            ArrayList<CommandAsyncTask> CurrentActionTypeTasks = getCurrentTasks();
            returnValue = CurrentActionTypeTasks.size();
        }

        return returnValue;
    }

    public void repoll() {
        synchronized (_listTasks) {
            ArrayList<CommandAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (CommandAsyncTask task : CurrentActionTypeTasks) {
                task.setState(AsyncTaskState.Ended);
            }
        }
    }

    public void clearTasks() {
        synchronized (_listTasks) {
            ArrayList<CommandAsyncTask> CurrentActionTypeTasks = getCurrentTasks();

            for (CommandAsyncTask task : CurrentActionTypeTasks) {
                if (task.getLastTaskStatus().getStatus() == AsyncTaskStatusEnum.finished) {
                    task.clearAsyncTask();
                    _listTasks.remove(task.getVdsmTaskId());
                }
            }
        }
    }

    public void clearTaskByVdsmTaskId(Guid vdsmTaskId) {
        synchronized (_listTasks) {
            _listTasks.remove(vdsmTaskId);
        }
    }

    public void startPollingTask(Guid TaskID) {
        synchronized (_listTasks) {
            if (_listTasks.containsKey(TaskID) && _listTasks.get(TaskID).getParameters() != null
                    && _listTasks.get(TaskID).getParameters().getDbAsyncTask() != null) {
                for (CommandAsyncTask task : _listTasks.values()) {
                    if (task.getParameters() != null && task.getParameters().getDbAsyncTask() != null) {
                        task.setState(AsyncTaskState.Polling);
                    }
                }
            } else {
                log.warn("CommandMultiAsyncTasks::startPollingTask: For some reason, task '{}' has no parameters or"
                                + " no DB task - we don't know its action type",
                        TaskID);
            }
        }
    }

    public boolean getAllCleared() {
        synchronized (_listTasks) {
            for (CommandAsyncTask task : _listTasks.values()) {
                if (!taskWasCleared(task)) {
                    log.info("[within thread]: Some of the tasks related to command id '{}' were not cleared yet"
                                    + " (Task id '{}' is in state '{}').",
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
}
