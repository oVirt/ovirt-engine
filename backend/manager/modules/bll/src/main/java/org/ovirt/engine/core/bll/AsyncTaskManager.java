package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.tasks.AsyncTaskUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTasks;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtil;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * AsyncTaskManager: Singleton, manages all tasks in the system.
 */
public final class AsyncTaskManager {
    private static final Log log = LogFactory.getLog(AsyncTaskManager.class);

    /** Map which consist all tasks that currently are monitored **/
    private ConcurrentMap<Guid, SPMAsyncTask> _tasks;

    /** Indication if _tasks has changed for logging process. **/
    private boolean logChangedMap = true;

    /** The period of time (in minutes) to hold the asynchronous tasks' statuses in the asynchronous tasks cache **/
    private final int _cacheTimeInMinutes;

    /**Map of tasks in DB per storage pool that exist after restart **/
    private ConcurrentMap<Guid, List<AsyncTasks>> tasksInDbAfterRestart = null;



    private static final AsyncTaskManager _taskManager = new AsyncTaskManager();

    public static AsyncTaskManager getInstance() {
        return _taskManager;
    }


    private AsyncTaskManager() {
        _tasks = new ConcurrentHashMap<Guid, SPMAsyncTask>();

        SchedulerUtil scheduler = SchedulerUtilQuartzImpl.getInstance();
        scheduler.scheduleAFixedDelayJob(this, "_timer_Elapsed", new Class[] {},
                new Object[] {}, Config.<Integer> GetValue(ConfigValues.AsyncTaskPollingRate),
                Config.<Integer> GetValue(ConfigValues.AsyncTaskPollingRate), TimeUnit.SECONDS);

        scheduler.scheduleAFixedDelayJob(this, "_cacheTimer_Elapsed", new Class[] {},
                new Object[] {}, Config.<Integer> GetValue(ConfigValues.AsyncTaskStatusCacheRefreshRateInSeconds),
                Config.<Integer> GetValue(ConfigValues.AsyncTaskStatusCacheRefreshRateInSeconds), TimeUnit.SECONDS);
        _cacheTimeInMinutes = Config.<Integer> GetValue(ConfigValues.AsyncTaskStatusCachingTimeInMinutes);
        tasksInDbAfterRestart = new ConcurrentHashMap<Guid, List<AsyncTasks>>();
        for (AsyncTasks task: DbFacade.getInstance().getAsyncTaskDao().getAll()) {
            tasksInDbAfterRestart.putIfAbsent(task.getStoragePoolId(), new ArrayList<AsyncTasks>());
            List<AsyncTasks> tasksPerStoragePool = tasksInDbAfterRestart.get(task.getStoragePoolId());
            tasksInDbAfterRestart.put(task.getStoragePoolId(), tasksPerStoragePool);
            tasksPerStoragePool.add(task);
        }
    }

    public void InitAsyncTaskManager() {
        log.info("Initialization of AsyncTaskManager completed successfully.");
    }

    @OnTimerMethodAnnotation("_timer_Elapsed")
    public synchronized void _timer_Elapsed() {
        if (ThereAreTasksToPoll()) {
            PollAndUpdateAsyncTasks();

            if (ThereAreTasksToPoll() && logChangedMap) {
                log.infoFormat("Finished polling Tasks, will poll again in {0} seconds.",
                               Config.<Integer> GetValue(ConfigValues.AsyncTaskPollingRate));

                // Set indication to false for not logging the same message next
                // time.
                logChangedMap = false;
            }

            // check for zombie tasks
            if (_tasks.size() > 0) {
                cleanZombieTasks();
            }
        }
    }

    @OnTimerMethodAnnotation("_cacheTimer_Elapsed")
    public void _cacheTimer_Elapsed() {
        removeClearedAndOldTasks();
    }

    /**
     * Check if task should be cached or not. Task should not be cached , only
     * if the task has been rolled back (Cleared) or failed to be rolled back
     * (ClearedFailed), and been in that status for several minutes
     * (_cacheTimeInMinutes).
     *
     * @param task
     *            - Asynchronous task we check to cache or not.
     * @return - true for uncached object , and false when the object should be
     *         cached.
     */
    public synchronized boolean CachingOver(SPMAsyncTask task) {
        // Get time in milliseconds that the task should be cached
        long SubtractMinutesAsMills = TimeUnit.MINUTES
                .toMillis(_cacheTimeInMinutes);

        // check if task has been rolled back (Cleared) or failed to be rolled
        // back (ClearedFailed)
        // for SubtractMinutesAsMills of minutes.
        return (task.getState() == AsyncTaskState.Cleared || task.getState() == AsyncTaskState.ClearFailed)
                && task.getLastAccessToStatusSinceEnd() < (System
                        .currentTimeMillis() - SubtractMinutesAsMills);
    }

    public synchronized boolean HasTasksByStoragePoolId(Guid storagePoolID) {
        boolean retVal = false;
        if (_tasks != null) {
            for (SPMAsyncTask task : _tasks.values()) {
                if (task.getStoragePoolID().equals(storagePoolID)) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    public synchronized boolean hasTasksForEntityIdAndAction(Guid id, VdcActionType type) {
        boolean retVal = false;
        if (_tasks != null) {
            for (SPMAsyncTask task : _tasks.values()) {
                if (isCurrentTaskLookedFor(id, task)
                        && type.equals(task.getParameters().getDbAsyncTask().getaction_type())) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    private boolean isCurrentTaskLookedFor(Guid id, SPMAsyncTask task) {
        return (task instanceof EntityAsyncTask) && id.equals(task.getParameters().getEntityId())
                && (task.getState() != AsyncTaskState.Cleared)
                && (task.getState() != AsyncTaskState.ClearFailed);
    }

    private void cleanZombieTasks() {
        long maxTime = DateTime.getNow()
                .AddMinutes((-1) * Config.<Integer> GetValue(ConfigValues.AsyncTaskZombieTaskLifeInMinutes)).getTime();
        for (SPMAsyncTask task : _tasks.values()) {

            if (task.getParameters().getDbAsyncTask().getStartTime().getTime() < maxTime) {
                AuditLogableBase logable = new AuditLogableBase();
                logable.addCustomValue("CommandName", task.getParameters().getDbAsyncTask().getaction_type().toString());
                logable.addCustomValue("Date", task.getParameters().getDbAsyncTask().getStartTime().toString());

                // if task is not finish and not unknown then it's in running
                // status
                if (task.getLastTaskStatus().getStatus() != AsyncTaskStatusEnum.finished
                        && task.getLastTaskStatus().getStatus() != AsyncTaskStatusEnum.unknown) {
                    AuditLogDirector.log(logable, AuditLogType.TASK_STOPPING_ASYNC_TASK);

                    log.infoFormat("Cleaning zombie tasks: Stopping async task {0} that started at {1}",
                            task.getParameters().getDbAsyncTask().getaction_type(), task
                                    .getParameters().getDbAsyncTask().getStartTime());

                    task.stopTask(true);
                } else {
                    AuditLogDirector.log(logable, AuditLogType.TASK_CLEARING_ASYNC_TASK);

                    log.infoFormat("Cleaning zombie tasks: Clearing async task {0} that started at {1}",
                            task.getParameters().getDbAsyncTask().getaction_type(), task
                                    .getParameters().getDbAsyncTask().getStartTime());

                    task.clearAsyncTask(true);
                }
            }
        }
    }

    private int NumberOfTasksToPoll() {
        int retValue = 0;
        for (SPMAsyncTask task : _tasks.values()) {
            if (task.getShouldPoll()) {
                retValue++;
            }
        }

        return retValue;
    }

    private boolean ThereAreTasksToPoll() {
        for (SPMAsyncTask task : _tasks.values()) {
            if (task.getShouldPoll()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fetch all tasks statuses from each storagePoolId , and update the _tasks
     * map with the updated statuses.
     */
    private void PollAndUpdateAsyncTasks() {
        if (logChangedMap) {
            log.infoFormat("Polling and updating Async Tasks: {0} tasks, {1} tasks to poll now",
                           _tasks.size(), NumberOfTasksToPoll());
        }

        // Fetch Set of pool id's
        Set<Guid> poolsOfActiveTasks = getPoolIdsTasks();

        // Get all tasks from all the SPMs.
        Map<Guid, Map<Guid, AsyncTaskStatus>> poolsAllTasksMap = getSPMsTasksStatuses(poolsOfActiveTasks);

        // For each task that found on each pool id
        updateTaskStatuses(poolsAllTasksMap);
    }

    /**
     * Update task status based on asyncTaskMap.
     *
     * @param asyncTaskMap
     *            - Task statuses Map fetched from VDSM.
     */
    private void updateTaskStatuses(
                                    Map<Guid, Map<Guid, AsyncTaskStatus>> poolsAllTasksMap) {
        for (SPMAsyncTask task : _tasks.values()) {
            if (task.getShouldPoll()) {
                Map<Guid, AsyncTaskStatus> asyncTasksForPoolMap = poolsAllTasksMap
                        .get(task.getStoragePoolID());

                // If the storage pool id exists
                if (asyncTasksForPoolMap != null) {
                    AsyncTaskStatus cachedAsyncTaskStatus = asyncTasksForPoolMap
                            .get(task.getTaskID());

                    // task found in VDSM.
                    task.UpdateTask(cachedAsyncTaskStatus);
                }
            }
        }

    }

    /**
     * Call VDSCommand for each pool id fetched from poolsOfActiveTasks , and
     * Initialize a map with each storage pool Id task statuses.
     *
     * @param poolsOfActiveTasks
     *            - Set of all the active tasks fetched from _tasks.
     * @return poolsAsyncTaskMap - Map which contains tasks for each storage
     *         pool id.
     */
    @SuppressWarnings("unchecked")
    private Map<Guid, Map<Guid, AsyncTaskStatus>> getSPMsTasksStatuses(Set<Guid> poolsOfActiveTasks) {
        Map<Guid, Map<Guid, AsyncTaskStatus>> poolsAsyncTaskMap = new HashMap<Guid, Map<Guid, AsyncTaskStatus>>();

        // For each pool Id (SPM) ,add its tasks to the map.
        for (Guid storagePoolID : poolsOfActiveTasks) {
            try {
                Map<Guid, AsyncTaskStatus> map =
                        (Map<Guid, AsyncTaskStatus>) Backend.getInstance().getResourceManager().RunVdsCommand(
                                VDSCommandType.SPMGetAllTasksStatuses,
                                new IrsBaseVDSCommandParameters(storagePoolID)).getReturnValue();
                if (map != null) {
                    poolsAsyncTaskMap.put(storagePoolID, map);
                }
            } catch (RuntimeException e) {
                if ((e instanceof VdcBLLException) &&
                        (((VdcBLLException) e).getErrorCode() == VdcBllErrors.VDS_NETWORK_ERROR)) {
                    log.debugFormat("Get SPM task statuses: Calling Command {1}{2}, " +
                            "with storagePoolId {3}) threw an exception.",
                            VDSCommandType.SPMGetAllTasksStatuses, "VDSCommand", storagePoolID);
                } else {
                    log.debugFormat("Get SPM task statuses: Calling Command {1}{2}, " +
                            "with storagePoolId {3}) threw an exception.",
                            VDSCommandType.SPMGetAllTasksStatuses, "VDSCommand", storagePoolID, e);
                }
            }
        }

        return poolsAsyncTaskMap;
    }

    /**
     * Get a Set of all the storage pool id's of tasks that should pool.
     *
     * @see org.ovirt.engine.core.bll.SPMAsyncTask#getShouldPoll()
     * @return - Set of active tasks.
     */
    private Set<Guid> getPoolIdsTasks() {
        Set<Guid> poolsOfActiveTasks = new HashSet<Guid>();

        for (SPMAsyncTask task : _tasks.values()) {
            if (task.getShouldPoll()) {
                poolsOfActiveTasks.add(task.getStoragePoolID());
            }
        }
        return poolsOfActiveTasks;
    }

    /**
     * get list of pools that have only cleared and old tasks (which don't exist
     * anymore in the manager):
     *
     * @return
     */
    synchronized private void removeClearedAndOldTasks() {
        Set<Guid> poolsOfActiveTasks = new HashSet<Guid>();
        Set<Guid> poolsOfClearedAndOldTasks = new HashSet<Guid>();
        ConcurrentMap<Guid, SPMAsyncTask> activeTaskMap = new ConcurrentHashMap<Guid, SPMAsyncTask>();
        for (SPMAsyncTask task : _tasks.values()) {
            if (!CachingOver(task)) {
                activeTaskMap.put(task.getTaskID(), task);
                poolsOfActiveTasks.add(task.getStoragePoolID());
            } else {
                poolsOfClearedAndOldTasks.add(task.getStoragePoolID());
            }
        }

        // Check if _tasks need to be updated with less tasks (activated tasks).
        if (poolsOfClearedAndOldTasks.size() > 0) {
            setNewMap(activeTaskMap);
            poolsOfClearedAndOldTasks.removeAll(poolsOfActiveTasks);
        }
        for (Guid storagePoolID : poolsOfClearedAndOldTasks) {
            log.infoFormat("Cleared all tasks of pool {0}.",
                    storagePoolID);
        }
    }

    public synchronized void lockAndAddTaskToManager(SPMAsyncTask task) {
        addTaskToManager(task);
    }

    private void addTaskToManager(SPMAsyncTask task) {
        if (task == null) {
            log.error("Cannot add a null task.");
        }

        else {
            if (!_tasks.containsKey(task.getTaskID())) {
                log.infoFormat(
                        "Adding task '{0}' (Parent Command {1}, Parameters Type {2}), {3}.",
                        task.getTaskID(),
                        (task.getParameters().getDbAsyncTask().getaction_type()),
                        task.getParameters().getClass().getName(),
                        (task.getShouldPoll() ? "polling started."
                                : "polling hasn't started yet."));

                // Set the indication to true for logging _tasks status on next
                // quartz execution.
                AddTaskToMap(task.getTaskID(), task);
            } else {
                SPMAsyncTask existingTask = _tasks.get(task.getTaskID());
                if (existingTask.getParameters().getDbAsyncTask().getaction_type() == VdcActionType.Unknown
                        && task.getParameters().getDbAsyncTask().getaction_type() != VdcActionType.Unknown) {
                    log.infoFormat(
                            "Task '{0}' already exists with action type 'Unknown', now overriding it with action type '{1}'",
                            task.getTaskID(),
                            task.getParameters().getDbAsyncTask().getaction_type());

                    // Set the indication to true for logging _tasks status on
                    // next quartz execution.
                    AddTaskToMap(task.getTaskID(), task);
                }
            }
        }
    }

    /**
     * Adds new task to _tasks map , and set the log status to true. We set the
     * indication to true for logging _tasks status on next quartz execution.
     *
     * @param guid
     *            - Key of the map.
     * @param asyncTask
     *            - Value of the map.
     */
    private void AddTaskToMap(Guid guid, SPMAsyncTask asyncTask) {
        _tasks.put(guid, asyncTask);
        logChangedMap = true;
    }

    /**
     * We set the indication to true when _tasks map changes for logging _tasks
     * status on next quartz execution.
     *
     * @param asyncTaskMap
     *            - Map to copy to _tasks map.
     */
    private void setNewMap(ConcurrentMap<Guid, SPMAsyncTask> asyncTaskMap) {
        // If not the same set _tasks to be as asyncTaskMap.
        _tasks = asyncTaskMap;

        // Set the indication to true for logging.
        logChangedMap = true;

        // Log tasks to poll now.
        log.infoFormat("Setting new tasks map. The map contains now {0} tasks", _tasks.size());
    }

    public SPMAsyncTask CreateTask(AsyncTaskType taskType, AsyncTaskParameters taskParameters) {
        return AsyncTaskFactory.Construct(taskType, taskParameters, false);
    }

    public synchronized void StartPollingTask(Guid taskID) {
        if (_tasks.containsKey(taskID)) {
            _tasks.get(taskID).StartPollingTask();
        }
    }

    public synchronized ArrayList<AsyncTaskStatus> PollTasks(java.util.ArrayList<Guid> taskIdList) {
        ArrayList<AsyncTaskStatus> returnValue = new ArrayList<AsyncTaskStatus>();

        if (taskIdList != null && taskIdList.size() > 0) {
            for (Guid taskId : taskIdList) {
                if (_tasks.containsKey(taskId)) {
                    // task is still running or is still in the cache:
                    _tasks.get(taskId).setLastStatusAccessTime();
                    returnValue.add(_tasks.get(taskId).getLastTaskStatus());
                }

                else
                // task doesn't exist in the manager (shouldn't happen) ->
                // assume it has been ended successfully.
                {
                    log.warnFormat(
                            "Polling tasks. Task ID '{0}' doesn't exist in the manager -> assuming 'finished'.",
                            taskId);

                    AsyncTaskStatus tempVar = new AsyncTaskStatus();
                    tempVar.setStatus(AsyncTaskStatusEnum.finished);
                    tempVar.setResult(AsyncTaskResultEnum.success);
                    returnValue.add(tempVar);
                }
            }
        }

        return returnValue;
    }

    /**
     * Retrieves from the specified storage pool the tasks that exist on it and
     * adds them to the manager.
     *
     * @param sp
     *            the storage pool to retrieve running tasks from
     */
    public void AddStoragePoolExistingTasks(storage_pool sp) {
        List<AsyncTaskCreationInfo> currPoolTasks = null;
        try {
            currPoolTasks = (ArrayList<AsyncTaskCreationInfo>) Backend.getInstance().getResourceManager()
                    .RunVdsCommand(VDSCommandType.SPMGetAllTasksInfo, new IrsBaseVDSCommandParameters(sp.getId()))
                    .getReturnValue();
        } catch (RuntimeException e) {
            log.error(
                    String.format(
                            "Getting existing tasks on Storage Pool %1$s failed.",
                            sp.getname()),
                    e);
        }

        if (currPoolTasks != null && currPoolTasks.size() > 0) {
            synchronized (this) {
                final List<SPMAsyncTask> newlyAddedTasks = new ArrayList<SPMAsyncTask>();

                for (AsyncTaskCreationInfo creationInfo : currPoolTasks) {
                    creationInfo.setStoragePoolID(sp.getId());
                    if (!_tasks.containsKey(creationInfo.getTaskID())) {
                        try {
                            SPMAsyncTask task = AsyncTaskFactory.Construct(creationInfo);
                            addTaskToManager(task);
                            newlyAddedTasks.add(task);
                        } catch (Exception e) {
                            log.errorFormat("Failed to load task of type {0} with id {1}, due to: {2}.",
                                       creationInfo.getTaskType(), creationInfo.getTaskID(),
                                       ExceptionUtils.getRootCauseMessage(e));
                        }
                    }
                }

                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

                    @Override
                    public Void runInTransaction() {
                        for (SPMAsyncTask task : newlyAddedTasks) {
                            AsyncTaskUtils.addOrUpdateTaskInDB(task);
                        }
                        return null;
                    }
                });

                for (SPMAsyncTask task : newlyAddedTasks) {
                    StartPollingTask(task.getTaskID());
                }

                log.infoFormat(
                        "Discovered {0} tasks on Storage Pool '{1}', {2} added to manager.",
                        currPoolTasks.size(),
                        sp.getname(),
                        newlyAddedTasks.size());
            }
        }

        else {
            log.infoFormat("Discovered no tasks on Storage Pool {0}",
                    sp.getname());
        }


        List<AsyncTasks> tasksInDForStoragePool = tasksInDbAfterRestart.get(sp.getId());
        if (tasksInDForStoragePool != null) {
            for (AsyncTasks task : tasksInDForStoragePool) {
                if (!_tasks.containsKey(task.gettask_id())) {
                    DbFacade.getInstance().getAsyncTaskDao().remove(task.gettask_id());
                }
            }
        }
        //Either the tasks were only in DB - so they were removed from db, or they are polled -
        //in any case no need to hold them in the map that represents the tasksInDbAfterRestart
        tasksInDbAfterRestart.remove(sp.getId());

    }

    /**
     * Retrieves all tasks from the specified storage pool and stops them.
     *
     * @param sp
     */
    public synchronized void StopStoragePoolTasks(final storage_pool sp) {
        log.infoFormat("Attempting to get and stop tasks on storage pool '{0}'",
                sp.getname());

        AddStoragePoolExistingTasks(sp);

        List<SPMAsyncTask> list = LinqUtils.filter(_tasks.values(), new Predicate<SPMAsyncTask>() {
            @Override
            public boolean eval(SPMAsyncTask a) {
                return a.getStoragePoolID().equals(sp.getId());
            }
        });
        for (SPMAsyncTask task : list) {
            task.stopTask();
        }
    }

    /**
     * Stops all tasks, and set them to polling state, for clearing them up later.
     *
     * @param taskList
     *            - List of tasks to stop.
     */
    public synchronized void CancelTasks(List<Guid> taskList) {
        for (Guid taskID : taskList) {
            CancelTask(taskID);
        }
    }

    public synchronized void CancelTask(Guid taskID) {
        if (_tasks.containsKey(taskID)) {
            log.infoFormat("Attempting to cancel task '{0}'.", taskID);
            _tasks.get(taskID).stopTask();
            _tasks.get(taskID).ConcreteStartPollingTask();
        }
    }

    public synchronized boolean EntityHasTasks(Guid id) {
        for (SPMAsyncTask task : _tasks.values()) {
            if (isCurrentTaskLookedFor(id, task)) {
                return true;
            }
        }
        return false;
    }

    public synchronized boolean StoragePoolHasUnclearedTasks(Guid storagePoolId) {
        for (SPMAsyncTask task : _tasks.values()) {
            if (task.getState() != AsyncTaskState.Cleared && task.getState() != AsyncTaskState.ClearFailed
                    && task.getStoragePoolID().equals(storagePoolId)) {
                return true;
            }
        }

        return false;
    }

}
