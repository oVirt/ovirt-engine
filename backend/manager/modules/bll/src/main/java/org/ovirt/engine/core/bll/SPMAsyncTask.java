package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.config.ConfigValues.UknownTaskPrePollingLapse;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.common.vdscommands.SPMTaskGuidBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class SPMAsyncTask {
    public SPMAsyncTask(AsyncTaskParameters parameters) {
        setParameters(parameters);
        setState(AsyncTaskState.Initializing);
    }

    private AsyncTaskParameters privateParameters;
    private Guid[] associatedEntities;
    private VdcObjectType entityType;

    public void setAssociatedEntities(Guid[] associatedEntities) {
        this.associatedEntities = associatedEntities;
    }

    public Guid[] getAssociatedEntities() {
        return associatedEntities;
    }

    public VdcObjectType getEntityType() {
        return entityType;
    }

    public void setEntityType(VdcObjectType entityType) {
        this.entityType = entityType;
    }


    public AsyncTaskParameters getParameters() {
        return privateParameters;
    }

    public void setParameters(AsyncTaskParameters value) {
        privateParameters = value;
    }

    public Guid getVdsmTaskId() {
        return getParameters().getVdsmTaskId();
    }

    public Guid getStoragePoolID() {
        return getParameters().getStoragePoolID();
    }

    private AsyncTaskState privateState = AsyncTaskState.forValue(0);

    public AsyncTaskState getState() {
        return privateState;
    }

    public void setState(AsyncTaskState value) {
        privateState = value;
    }

    public boolean getShouldPoll() {
        AsyncTaskState state = getState();
        return (state == AsyncTaskState.Polling || state == AsyncTaskState.Ended || state == AsyncTaskState.ClearFailed)
                && getLastTaskStatus().getStatus() != AsyncTaskStatusEnum.unknown
                && (getParameters().getEntityId() == null ? isTaskOverPrePollingLapse() : true);
    }

    private AsyncTaskStatus _lastTaskStatus = new AsyncTaskStatus(AsyncTaskStatusEnum.init);

    public AsyncTaskStatus getLastTaskStatus() {
        return _lastTaskStatus;
    }

    /**
     * Set the _lastTaskStatus with taskStatus.
     *
     * @param taskStatus
     *            - task status to set.
     */
    protected void setLastTaskStatus(AsyncTaskStatus taskStatus) {
        _lastTaskStatus = taskStatus;
    }

    /**
     * Update task last access date ,only for not active task.
     */
    public void setLastStatusAccessTime() {
        // Change access date to now , when task is not active.
        if (getState() == AsyncTaskState.Ended
                || getState() == AsyncTaskState.AttemptingEndAction
                || getState() == AsyncTaskState.ClearFailed
                || getState() == AsyncTaskState.Cleared) {
            _lastAccessToStatusSinceEnd = System.currentTimeMillis();
        }
    }

    // Indicates time in milliseconds when task status recently changed.
    protected long _lastAccessToStatusSinceEnd = System.currentTimeMillis();

    public long getLastAccessToStatusSinceEnd() {
        return _lastAccessToStatusSinceEnd;
    }

    public Object getContainerId() {
        return getParameters().getEntityId();
    }

    public void StartPollingTask() {
        AsyncTaskState state = getState();
        if (state != AsyncTaskState.AttemptingEndAction
                && state != AsyncTaskState.Cleared
                && state != AsyncTaskState.ClearFailed) {
            log.infoFormat("BaseAsyncTask::StartPollingTask: Starting to poll task '{0}'.", getVdsmTaskId());
            ConcreteStartPollingTask();
        }
    }

    /**
     * Use this to hold unknown tasks from polling, to overcome bz673695 without a complete re-haul to the
     * AsyncTaskManager and CommandBase.
     * @TODO remove this and re-factor {@link AsyncTaskManager}
     * @return true when the time passed after creating the task is bigger than
     *         <code>ConfigValues.UknownTaskPrePollingLapse</code>
     * @see AsyncTaskManager
     * @see CommandBase
     * @since 3.0
     */
    boolean isTaskOverPrePollingLapse() {
        AsyncTaskParameters parameters = getParameters();
        long taskStartTime = parameters.getDbAsyncTask().getStartTime().getTime();
        Integer prePollingPeriod = Config.<Integer> GetValue(UknownTaskPrePollingLapse);
        boolean idlePeriodPassed =
                System.currentTimeMillis() - taskStartTime > prePollingPeriod;

        log.infoFormat("task id {0} {1}. Pre-polling period is {2} millis. ",
                parameters.getVdsmTaskId(),
                idlePeriodPassed ? "has passed pre-polling period time and should be polled"
                        : "is in pre-polling  period and should not be polled", prePollingPeriod);
        return idlePeriodPassed;
    }

    protected void ConcreteStartPollingTask() {
        setState(AsyncTaskState.Polling);
    }

    /**
     * For each task set its updated status retrieved from VDSM.
     *
     * @param returnTaskStatus
     *            - Task status returned from VDSM.
     */
    @SuppressWarnings("incomplete-switch")
    public void UpdateTask(AsyncTaskStatus returnTaskStatus) {
        try {
            switch (getState()) {
            case Polling:
                // Get the returned task
                returnTaskStatus = CheckTaskExist(returnTaskStatus);
                if (returnTaskStatus.getStatus() != getLastTaskStatus().getStatus()) {
                    AddLogStatusTask(returnTaskStatus);
                }
                setLastTaskStatus(returnTaskStatus);

                if (!getLastTaskStatus().getTaskIsRunning()) {
                    HandleEndedTask();
                }
                break;

            case Ended:
                HandleEndedTask();
                break;

            // Try to clear task which failed to be cleared before SPM and DB
            case ClearFailed:
                clearAsyncTask();
                break;
            }
        }

        catch (RuntimeException e) {
            log.error(
                    String.format(
                            "BaseAsyncTask::PollAndUpdateTask: Handling task '%1$s' (State: %2$s, Parent Command: %3$s, Parameters Type: %4$s) threw an exception",
                            getVdsmTaskId(),
                            getState(),
                            (getParameters().getDbAsyncTask()
                                    .getaction_type()),
                            getParameters()
                                    .getClass().getName()),
                    e);
        }
    }

    /**
     * Handle ended task operation. Change task state to Ended ,Cleared or
     * Cleared Failed , and log appropriate message.
     */
    private void HandleEndedTask() {
        // If task state is different from Ended change it to Ended and set the
        // last access time to now.
        if (getState() != AsyncTaskState.Ended) {
            setState(AsyncTaskState.Ended);
            setLastStatusAccessTime();
        }

        if (HasTaskEndedSuccessfully()) {
            ExecutionHandler.endTaskStep(privateParameters.getDbAsyncTask().getStepId(), JobExecutionStatus.FINISHED);
            OnTaskEndSuccess();
        }

        else if (HasTaskEndedInFailure()) {
            ExecutionHandler.endTaskStep(privateParameters.getDbAsyncTask().getStepId(), JobExecutionStatus.FAILED);
            OnTaskEndFailure();
        }

        else if (!DoesTaskExist()) {
            ExecutionHandler.endTaskStep(privateParameters.getDbAsyncTask().getStepId(), JobExecutionStatus.UNKNOWN);
            OnTaskDoesNotExist();
        }
    }

    protected void RemoveTaskFromDB() {
        try {
            if (DbFacade.getInstance().getAsyncTaskDao().removeByVdsmTaskId(getVdsmTaskId()) != 0) {
                log.infoFormat("BaseAsyncTask::RemoveTaskFromDB: Removed task {0} from DataBase", getVdsmTaskId());
            }
        }

        catch (RuntimeException e) {
            log.error(String.format(
                    "BaseAsyncTask::RemoveTaskFromDB: Removing task %1$s from DataBase threw an exception.",
                    getVdsmTaskId()), e);
        }
    }

    private boolean HasTaskEndedSuccessfully() {
        return getLastTaskStatus().getTaskEndedSuccessfully();
    }

    private boolean HasTaskEndedInFailure() {
        return !getLastTaskStatus().getTaskIsRunning() && !getLastTaskStatus().getTaskEndedSuccessfully();
    }

    private boolean DoesTaskExist() {
        return getLastTaskStatus().getStatus() != AsyncTaskStatusEnum.unknown;
    }

    protected void OnTaskEndSuccess() {
        LogEndTaskSuccess();
        clearAsyncTask();
    }

    protected void LogEndTaskSuccess() {
        log.infoFormat(
                "BaseAsyncTask::OnTaskEndSuccess: Task '{0}' (Parent Command {1}, Parameters Type {2}) ended successfully.",
                getVdsmTaskId(),
                (getParameters().getDbAsyncTask().getaction_type()),
                getParameters()
                        .getClass().getName());
    }

    protected void OnTaskEndFailure() {
        LogEndTaskFailure();
        clearAsyncTask();
    }

    protected void LogEndTaskFailure() {
        log.errorFormat(
                "BaseAsyncTask::LogEndTaskFailure: Task '{0}' (Parent Command {1}, Parameters Type {2}) ended with failure:"
                        + "\r\n" + "-- Result: '{3}'" + "\r\n" + "-- Message: '{4}'," + "\r\n" + "-- Exception: '{5}'",
                getVdsmTaskId(),
                (getParameters().getDbAsyncTask().getaction_type()),
                getParameters()
                        .getClass().getName(),
                getLastTaskStatus().getResult(),
                (getLastTaskStatus().getMessage() == null ? "[null]" : getLastTaskStatus().getMessage()),
                (getLastTaskStatus()
                        .getException() == null ? "[null]" : getLastTaskStatus().getException().getMessage()));
    }

    protected void OnTaskDoesNotExist() {
        LogTaskDoesntExist();
        clearAsyncTask();
    }

    protected void LogTaskDoesntExist() {
        log.errorFormat(
                "BaseAsyncTask::LogTaskDoesntExist: Task '{0}' (Parent Command {1}, Parameters Type {2}) does not exist.",
                getVdsmTaskId(),
                (getParameters().getDbAsyncTask().getaction_type()),
                getParameters()
                        .getClass().getName());
    }

    /**
     * Print log message, Checks if the cachedStatusTask is null, (indicating the task was not found in the SPM).
     * If so returns {@link AsyncTaskStatusEnum#unknown} status, otherwise returns the status as given.<br>
     * <br>
     * @param cachedStatusTask The status from the SPM, or <code>null</code> is the task wasn't found in the SPM.
     * @return - Updated status task
     */
    protected AsyncTaskStatus CheckTaskExist(AsyncTaskStatus cachedStatusTask) {
        AsyncTaskStatus returnedStatusTask = null;

        // If the cachedStatusTask is null ,that means the task has not been found in the SPM.
        if (cachedStatusTask == null) {
            // Set to running in order to continue polling the task in case SPM hasn't loaded the tasks yet..
            returnedStatusTask = new AsyncTaskStatus(AsyncTaskStatusEnum.unknown);

            log.errorFormat("SPMAsyncTask::PollTask: Task '{0}' (Parent Command {1}, Parameters Type {2}) " +
                        "was not found in VDSM, will change its status to unknown.",
                        getVdsmTaskId(), (getParameters().getDbAsyncTask().getaction_type()),
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
    protected void AddLogStatusTask(AsyncTaskStatus cachedStatusTask) {

        String formatString = "SPMAsyncTask::PollTask: Polling task '{0}' (Parent Command {1}, Parameters Type {2}) "
                + "returned status '{3}'{4}.";

        // If task doesn't exist (unknown) or has ended with failure (aborting)
        // , log warn.
        if (cachedStatusTask.getTaskIsInUnusualState()) {
            log.warnFormat(
                    formatString,
                    getVdsmTaskId(),
                    (getParameters().getDbAsyncTask()
                            .getaction_type()),
                    getParameters().getClass().getName(),
                    cachedStatusTask.getStatus(),
                    ((cachedStatusTask.getStatus() == AsyncTaskStatusEnum.finished) ? (String
                            .format(", result '%1$s'",
                                    cachedStatusTask.getResult())) : ("")));
        }

        else {
            log.infoFormat(
                    formatString,
                    getVdsmTaskId(),
                    (getParameters().getDbAsyncTask()
                            .getaction_type()),
                    getParameters().getClass().getName(),
                    cachedStatusTask.getStatus(),
                    ((cachedStatusTask.getStatus() == AsyncTaskStatusEnum.finished) ? (String
                            .format(", result '%1$s'",
                                    cachedStatusTask.getResult())) : ("")));
        }
    }

    public void stopTask() {
        stopTask(false);
    }

    public void stopTask(boolean forceFinish) {
        if (getState() != AsyncTaskState.AttemptingEndAction && getState() != AsyncTaskState.Cleared
                && getState() != AsyncTaskState.ClearFailed && !getLastTaskStatus().getTaskIsInUnusualState()) {
            try {
                log.infoFormat(
                        "SPMAsyncTask::StopTask: Attempting to stop task '{0}' (Parent Command {1}, Parameters Type {2}).",
                        getVdsmTaskId(),
                        (getParameters().getDbAsyncTask().getaction_type()),
                        getParameters().getClass().getName());

                Backend.getInstance()
                        .getResourceManager()
                        .RunVdsCommand(VDSCommandType.SPMStopTask,
                                new SPMTaskGuidBaseVDSCommandParameters(getStoragePoolID(), getVdsmTaskId()));
            } catch (RuntimeException e) {
                log.error(
                        String.format("SPMAsyncTask::StopTask: Stopping task '%1$s' threw an exception.", getVdsmTaskId()),
                        e);
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

    public void clearAsyncTask() {
        clearAsyncTask(false);
    }

    public void clearAsyncTask(boolean forceDelete) {
        VDSReturnValue vdsReturnValue = null;

        try {
            log.infoFormat("SPMAsyncTask::ClearAsyncTask: Attempting to clear task '{0}'", getVdsmTaskId());
            vdsReturnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(VDSCommandType.SPMClearTask,
                            new SPMTaskGuidBaseVDSCommandParameters(getStoragePoolID(), getVdsmTaskId()));
        }

        catch (RuntimeException e) {
            log.error(String.format("SPMAsyncTask::ClearAsyncTask: Clearing task '%1$s' threw an exception.",
                    getVdsmTaskId()), e);
        }

        boolean shouldGracefullyDeleteTask = false;
        if (!isTaskStateError(vdsReturnValue)) {
            if (vdsReturnValue == null || !vdsReturnValue.getSucceeded()) {
                setState(AsyncTaskState.ClearFailed);
                OnTaskCleanFailure();
            } else {
                setState(AsyncTaskState.Cleared);
                shouldGracefullyDeleteTask =  true;
            }
        }
        //A task should be removed from DB if forceDelete is set to true, or if it was cleared successfully.
        if (shouldGracefullyDeleteTask || forceDelete) {
            RemoveTaskFromDB();
        }
    }

    /**
     * Function return true if we got error 410 - which is SPM initializing and
     * we did not clear the task
     *
     * @param vdsReturnValue
     * @return
     */
    private boolean isTaskStateError(VDSReturnValue vdsReturnValue) {
        if (vdsReturnValue != null && vdsReturnValue.getVdsError() != null
                && vdsReturnValue.getVdsError().getCode() == VdcBllErrors.TaskStateError) {
            log.infoFormat(
                    "SPMAsyncTask::ClearAsyncTask: At time of attemp to clear task '{0}' the response code was {2} and message was {3}. Task will not be cleaned",
                    getVdsmTaskId(),
                    vdsReturnValue.getVdsError().getCode(),
                    vdsReturnValue.getVdsError().getMessage());
            return true;
        }
        return false;
    }

    protected void OnTaskCleanFailure() {
        LogTaskCleanFailure();
    }

    protected void LogTaskCleanFailure() {
        log.errorFormat("SPMAsyncTask::ClearAsyncTask: Clearing task '{0}' failed.", getVdsmTaskId());
    }

    private static final Log log = LogFactory.getLog(SPMAsyncTask.class);
}
