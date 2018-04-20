package org.ovirt.engine.core.bll.tasks;

import static org.ovirt.engine.core.common.config.ConfigValues.UnknownTaskPrePollingLapse;

import java.util.Map;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.bll.tasks.interfaces.SPMTask;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SPMAsyncTask implements SPMTask {
    private static final Logger log = LoggerFactory.getLogger(SPMAsyncTask.class);

    protected final CommandCoordinator coco;
    private boolean zombieTask;
    private AsyncTaskParameters parameters;
    private Map<Guid, VdcObjectType> entitiesMap;
    private AsyncTaskState state;
    private AsyncTaskStatus lastTaskStatus;
    private boolean partiallyCompletedCommandTask;

    // Indicates time in milliseconds when task status recently changed.
    private long lastAccessToStatusSinceEnd;

    public SPMAsyncTask(CommandCoordinator coco, AsyncTaskParameters parameters) {
        this.coco = coco;
        setParameters(parameters);
        setState(AsyncTaskState.Initializing);
        setLastTaskStatus(new AsyncTaskStatus(AsyncTaskStatusEnum.init));
        lastAccessToStatusSinceEnd = System.currentTimeMillis();
    }

    @Override
    public Map<Guid, VdcObjectType> getEntitiesMap() {
        return entitiesMap;
    }

    @Override
    public void setEntitiesMap(Map<Guid, VdcObjectType> entitiesMap) {
        this.entitiesMap = entitiesMap;
    }

    @Override
    public AsyncTaskParameters getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(AsyncTaskParameters parameters) {
        this.parameters = parameters;
    }

    @Override
    public Guid getVdsmTaskId() {
        return getParameters().getVdsmTaskId();
    }

    @Override
    public Guid getStoragePoolID() {
        return getParameters().getStoragePoolID();
    }

    @Override
    public AsyncTaskState getState() {
        return state;
    }

    @Override
    public void setState(AsyncTaskState state) {
        this.state = state;
    }

    @Override
    public boolean getShouldPoll() {
        AsyncTaskState state = getState();
        return (state == AsyncTaskState.Polling || state == AsyncTaskState.Ended || state == AsyncTaskState.ClearFailed)
                && getLastTaskStatus().getStatus() != AsyncTaskStatusEnum.unknown
                && (getParameters().getEntityInfo() == null ? isTaskOverPrePollingLapse() : true);
    }

    @Override
    public AsyncTaskStatus getLastTaskStatus() {
        return lastTaskStatus;
    }

    /**
     * Set the lastTaskStatus with taskStatus.
     *
     * @param lastTaskStatus
     *            - task status to set.
     */
    @Override
    public void setLastTaskStatus(AsyncTaskStatus lastTaskStatus) {
        this.lastTaskStatus = lastTaskStatus;
    }

    /**
     * Update task last access date ,only for not active task.
     */
    @Override
    public void setLastStatusAccessTime() {
        // Change access date to now , when task is not active.
        if (getState() == AsyncTaskState.Ended
                || getState() == AsyncTaskState.AttemptingEndAction
                || getState() == AsyncTaskState.ClearFailed
                || getState() == AsyncTaskState.Cleared) {
            lastAccessToStatusSinceEnd = System.currentTimeMillis();
        }
    }

    @Override
    public long getLastAccessToStatusSinceEnd() {
        return lastAccessToStatusSinceEnd;
    }

    @Override
    public Guid getCommandId() {
        return getParameters().getDbAsyncTask().getRootCommandId();
    }

    @Override
    public void startPollingTask() {
        AsyncTaskState state = getState();
        if (state != AsyncTaskState.AttemptingEndAction
                && state != AsyncTaskState.Cleared
                && state != AsyncTaskState.ClearFailed) {
            log.info("BaseAsyncTask::startPollingTask: Starting to poll task '{}'.", getVdsmTaskId());
            concreteStartPollingTask();
        }
    }

    /**
     * Use this to hold unknown tasks from polling, to overcome bz673695 without a complete re-haul to the
     * AsyncTaskManager and CommandBase.
     * TODO remove this and re-factor {@link org.ovirt.engine.core.bll.tasks.AsyncTaskManager}
     * @return true when the time passed after creating the task is bigger than
     *         <code>ConfigValues.UnknownTaskPrePollingLapse</code>
     * @see org.ovirt.engine.core.bll.tasks.AsyncTaskManager
     * @see org.ovirt.engine.core.bll.CommandBase
     * @since 3.0
     */
    boolean isTaskOverPrePollingLapse() {
        AsyncTaskParameters parameters = getParameters();
        long taskStartTime = parameters.getDbAsyncTask().getStartTime().getTime();
        Integer prePollingPeriod = Config.<Integer> getValue(UnknownTaskPrePollingLapse);
        boolean idlePeriodPassed =
                System.currentTimeMillis() - taskStartTime > prePollingPeriod;

        log.info("Task id '{}' {}. Pre-polling period is {} millis. ",
                parameters.getVdsmTaskId(),
                idlePeriodPassed ? "has passed pre-polling period time and should be polled"
                        : "is in pre-polling  period and should not be polled",
                prePollingPeriod);
        return idlePeriodPassed;
    }

    @Override
    public void concreteStartPollingTask() {
        setState(AsyncTaskState.Polling);
    }

    /**
     * For each task set its updated status retrieved from VDSM.
     *
     * @param returnTaskStatus
     *            - Task status returned from VDSM.
     */
    @Override
    @SuppressWarnings("incomplete-switch")
    public void updateTask(AsyncTaskStatus returnTaskStatus) {
        try {
            switch (getState()) {
            case Polling:
                // Get the returned task
                returnTaskStatus = checkTaskExist(returnTaskStatus);
                if (returnTaskStatus.getStatus() != getLastTaskStatus().getStatus()) {
                    addLogStatusTask(returnTaskStatus);
                }
                setLastTaskStatus(returnTaskStatus);

                if (!getLastTaskStatus().getTaskIsRunning()) {
                    handleEndedTask();
                }
                break;

            case Ended:
                handleEndedTask();
                break;

            // Try to clear task which failed to be cleared before SPM and DB
            case ClearFailed:
                clearAsyncTask();
                break;
            }
        } catch (RuntimeException e) {
            log.error("BaseAsyncTask::PollAndUpdateTask: Handling task '{}' (State '{}', Parent Command '{}'."
                            + " Parameters Type '{}') threw an exception",
                    getVdsmTaskId(),
                    getState(),
                    getParameters().getDbAsyncTask().getActionType(),
                    getParameters().getClass().getName());
            log.error("Exception", e);
        }
    }

    /**
     * Handle ended task operation. Change task state to Ended ,Cleared or
     * Cleared Failed , and log appropriate message.
     */
    private void handleEndedTask() {
        AsyncTask asyncTask = getParameters().getDbAsyncTask();

        log.debug("Task of command {} with id '{}' has ended.",
                asyncTask.getActionType(),
                getCommandId());

        // If task state is different from Ended change it to Ended and set the
        // last access time to now.
        if (getState() != AsyncTaskState.Ended) {
            setState(AsyncTaskState.Ended);
            setLastStatusAccessTime();
        }

        CommandEntity rootCmdEntity = coco.getCommandEntity(asyncTask.getRootCommandId());
        // if the task's root command has failed
        if (rootCmdEntity != null && !rootCmdEntity.isExecuted()) {
            // mark it as a task of a partially completed command
            // Will result in failure of the command
            setPartiallyCompletedCommandTask(true);
            log.debug("Marking task of command {} with id '{}' as partially completed.",
                    asyncTask.getActionType(),
                    getCommandId());
        }

        // Fail zombie task and task that belongs to a partially submitted command
        if (isZombieTask() || isPartiallyCompletedCommandTask()) {
            log.debug("Task of command {} with id '{}' is a zombie or is partially completed, executing failure logic.",
                    asyncTask.getActionType(),
                    getCommandId());
            getParameters().getDbAsyncTask().getTaskParameters().setTaskGroupSuccess(false);
            ExecutionHandler.getInstance().endTaskStep(parameters.getDbAsyncTask().getStepId(), JobExecutionStatus.FAILED);
            onTaskEndFailure();
        }

        if (hasTaskEndedSuccessfully()) {
            log.debug("Task of command {} with id '{}' has succeeded, executing success logic.",
                    asyncTask.getActionType(),
                    getCommandId());
            ExecutionHandler.getInstance().endTaskStep(parameters.getDbAsyncTask().getStepId(), JobExecutionStatus.FINISHED);
            onTaskEndSuccess();
        } else if (hasTaskEndedInFailure()) {
            log.debug("Task of command {} with id '{}' has failed, executing failure logic.",
                    asyncTask.getActionType(),
                    getCommandId());
            ExecutionHandler.getInstance().endTaskStep(parameters.getDbAsyncTask().getStepId(), JobExecutionStatus.FAILED);
            onTaskEndFailure();
        } else if (!doesTaskExist()) {
            log.debug("Task of command {} with id '{}' does not exist, executing cleanup logic.",
                    asyncTask.getActionType(),
                    getCommandId());
            ExecutionHandler.getInstance().endTaskStep(parameters.getDbAsyncTask().getStepId(), JobExecutionStatus.UNKNOWN);
            onTaskDoesNotExist();
        }
    }

    protected void removeTaskFromDB() {
        try {
            if (coco.removeByVdsmTaskId(getVdsmTaskId()) != 0) {
                log.info("BaseAsyncTask::removeTaskFromDB: Removed task '{}' from DataBase", getVdsmTaskId());
            }
        } catch (RuntimeException e) {
            log.error("BaseAsyncTask::removeTaskFromDB: Removing task '{}' from DataBase threw an exception: {}",
                    getVdsmTaskId(),
                    e.getMessage());
            log.debug("Exception", e);
        }
    }

    private boolean hasTaskEndedSuccessfully() {
        return getLastTaskStatus().getTaskEndedSuccessfully();
    }

    private boolean hasTaskEndedInFailure() {
        return !getLastTaskStatus().getTaskIsRunning() && !getLastTaskStatus().getTaskEndedSuccessfully();
    }

    private boolean doesTaskExist() {
        return getLastTaskStatus().getStatus() != AsyncTaskStatusEnum.unknown;
    }

    protected void onTaskEndSuccess() {
        logEndTaskSuccess();
        clearAsyncTask();
    }

    protected void logEndTaskSuccess() {
        log.info("BaseAsyncTask::onTaskEndSuccess: Task '{}' (Parent Command '{}', Parameters Type '{}')"
                        + " ended successfully.",
                getVdsmTaskId(),
                getParameters().getDbAsyncTask().getActionType(),
                getParameters().getClass().getName());
    }

    protected void onTaskEndFailure() {
        logEndTaskFailure();
        clearAsyncTask();
    }

    protected void logEndTaskFailure() {
        log.error("BaseAsyncTask::logEndTaskFailure: Task '{}' (Parent Command '{}', Parameters Type '{}')"
                        + " ended with failure:\n-- Result: '{}'\n-- Message: '{}',\n-- Exception: '{}'",
                getVdsmTaskId(),
                getParameters().getDbAsyncTask().getActionType(),
                getParameters().getClass().getName(),
                getLastTaskStatus().getResult(),
                getLastTaskStatus().getMessage() == null ? "[null]" : getLastTaskStatus().getMessage(),
                getLastTaskStatus().getException() == null ? "[null]" : getLastTaskStatus().getException().getMessage());
    }

    protected void onTaskDoesNotExist() {
        logTaskDoesntExist();
        clearAsyncTask();
    }

    protected void logTaskDoesntExist() {
        log.error("BaseAsyncTask::logTaskDoesntExist: Task '{}' (Parent Command '{}', Parameters Type '{}') does not exist.",
                getVdsmTaskId(),
                getParameters().getDbAsyncTask().getActionType(),
                getParameters().getClass().getName());
    }

    /**
     * Print log message, Checks if the cachedStatusTask is null, (indicating the task was not found in the SPM).
     * If so returns {@link AsyncTaskStatusEnum#unknown} status, otherwise returns the status as given.<br>
     * <br>
     * @param cachedStatusTask The status from the SPM, or <code>null</code> is the task wasn't found in the SPM.
     * @return - Updated status task
     */
    protected AsyncTaskStatus checkTaskExist(AsyncTaskStatus cachedStatusTask) {
        AsyncTaskStatus returnedStatusTask = null;

        // If the cachedStatusTask is null ,that means the task has not been found in the SPM.
        if (cachedStatusTask == null) {
            // Set to running in order to continue polling the task in case SPM hasn't loaded the tasks yet..
            returnedStatusTask = new AsyncTaskStatus(AsyncTaskStatusEnum.unknown);

            log.error("SPMAsyncTask::PollTask: Task '{}' (Parent Command '{}', Parameters Type '{}') "
                            + "was not found in VDSM, will change its status to unknown.",
                    getVdsmTaskId(),
                    getParameters().getDbAsyncTask().getActionType(),
                    getParameters().getClass().getName());
        } else {
            returnedStatusTask = cachedStatusTask;
        }
        return returnedStatusTask;
    }

    /**
     * Prints a log message of the task status,
     *
     * @param cachedStatusTask
     *            - Status got from VDSM
     */
    protected void addLogStatusTask(AsyncTaskStatus cachedStatusTask) {

        String formatString = "SPMAsyncTask::PollTask: Polling task '{}' (Parent Command '{}', Parameters Type '{}') "
                + "returned status '{}'{}.";

        // If task doesn't exist (unknown) or has ended with failure (aborting)
        // , log warn.
        if (cachedStatusTask.getTaskIsInUnusualState()) {
            log.warn(formatString,
                    getVdsmTaskId(),
                    getParameters().getDbAsyncTask().getActionType(),
                    getParameters().getClass().getName(),
                    cachedStatusTask.getStatus(),
                    cachedStatusTask.getStatus() == AsyncTaskStatusEnum.finished
                            ? String.format(", result '%1$s'", cachedStatusTask.getResult())
                            : "");
        } else {
            log.info(formatString,
                    getVdsmTaskId(),
                    getParameters().getDbAsyncTask().getActionType(),
                    getParameters().getClass().getName(),
                    cachedStatusTask.getStatus(),
                    cachedStatusTask.getStatus() == AsyncTaskStatusEnum.finished
                            ? String.format(", result '%1$s'", cachedStatusTask.getResult())
                            : "");
        }
    }

    @Override
    public void stopTask() {
        stopTask(false);
    }

    @Override
    public void stopTask(boolean forceFinish) {
        if (getState() != AsyncTaskState.AttemptingEndAction && getState() != AsyncTaskState.Cleared
                && getState() != AsyncTaskState.ClearFailed && !getLastTaskStatus().getTaskIsInUnusualState()) {
            try {
                log.info("SPMAsyncTask::StopTask: Attempting to stop task '{}' (Parent Command '{}', Parameters"
                                + " Type '{}').",
                        getVdsmTaskId(),
                        getParameters().getDbAsyncTask().getActionType(),
                        getParameters().getClass().getName());

                coco.stopTask(getStoragePoolID(), getVdsmTaskId());
            } catch (RuntimeException e) {
                log.error("SPMAsyncTask::StopTask: Error during stopping task '{}': {}",
                        getVdsmTaskId(),
                        e.getMessage());
                log.debug("Exception", e);
            } finally {
                if (forceFinish) {
                    //Force finish flag allows to force the task completion, regardless of the result from call to SPMStopTask
                    setState(AsyncTaskState.Ended);
                    setLastTaskStatus(new AsyncTaskStatus(AsyncTaskStatusEnum.finished));
                } else {
                    setState(AsyncTaskState.Polling);
                }
            }
        }
    }

    @Override
    public void clearAsyncTask() {
        // if we are calling updateTask on a task which has not been submitted,
        // to vdsm there is no need to clear the task. The task is just deleted
        // from the database
        if (Guid.Empty.equals(getVdsmTaskId())) {
            removeTaskFromDB();
            return;
        }
        clearAsyncTask(false);
    }

    @Override
    public void clearAsyncTask(boolean forceDelete) {
        VDSReturnValue vdsReturnValue = null;

        try {
            log.info("SPMAsyncTask::ClearAsyncTask: Attempting to clear task '{}'", getVdsmTaskId());
            vdsReturnValue = coco.clearTask(getStoragePoolID(), getVdsmTaskId());
        } catch (RuntimeException e) {
            log.error("SPMAsyncTask::ClearAsyncTask: Error during clearing task '{}': {}",
                    getVdsmTaskId(),
                    e.getMessage());
            log.error("Exception", e);
        }

        boolean shouldGracefullyDeleteTask = false;
        if (!isTaskStateError(vdsReturnValue)) {
            if (vdsReturnValue == null || !vdsReturnValue.getSucceeded()) {
                setState(AsyncTaskState.ClearFailed);
                onTaskCleanFailure();
            } else {
                setState(AsyncTaskState.Cleared);
                shouldGracefullyDeleteTask =  true;
            }
        }
        //A task should be removed from DB if forceDelete is set to true, or if it was cleared successfully.
        if (shouldGracefullyDeleteTask || forceDelete) {
            removeTaskFromDB();
        }
    }

    /**
     * Function return true if we got error 410 - which is SPM initializing and
     * we did not clear the task
     */
    private boolean isTaskStateError(VDSReturnValue vdsReturnValue) {
        if (vdsReturnValue != null && vdsReturnValue.getVdsError() != null
                && vdsReturnValue.getVdsError().getCode() == EngineError.TaskStateError) {
            log.info("SPMAsyncTask::ClearAsyncTask: At time of attempt to clear task '{}' the response code"
                            + " was '{}' and message was '{}'. Task will not be cleaned",
                    getVdsmTaskId(),
                    vdsReturnValue.getVdsError().getCode(),
                    vdsReturnValue.getVdsError().getMessage());
            return true;
        }
        return false;
    }

    protected void onTaskCleanFailure() {
        logTaskCleanFailure();
    }

    protected void logTaskCleanFailure() {
        log.error("SPMAsyncTask::ClearAsyncTask: Clearing task '{}' failed.", getVdsmTaskId());
    }

    @Override
    public boolean isPartiallyCompletedCommandTask() {
        return partiallyCompletedCommandTask;
    }

    @Override
    public void setPartiallyCompletedCommandTask(boolean val) {
        this.partiallyCompletedCommandTask = val;
    }

    @Override
    public boolean isZombieTask() {
        return zombieTask;
    }

    @Override
    public void setZombieTask(boolean zombieTask) {
        this.zombieTask = zombieTask;
    }
}
