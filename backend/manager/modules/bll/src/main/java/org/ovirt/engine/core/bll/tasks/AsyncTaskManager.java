package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.bll.tasks.interfaces.SPMTask;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.DateTime;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AsyncTaskManager: Singleton, manages all tasks in the system.
 */
@Singleton
public final class AsyncTaskManager implements BackendService {

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService schedulerService;

    @Inject
    private AsyncTaskFactory asyncTaskFactory;

    @Inject
    private AsyncTaskUtils asyncTaskUtils;

    @Inject
    private CommandCoordinator coco;

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskManager.class);
    private final AuditLogDirector auditLogDirector = Injector.get(AuditLogDirector.class);

    /** Map which consist all tasks that currently are monitored **/
    private ConcurrentMap<Guid, SPMTask> tasks;

    /** Indication if tasks has changed for logging process. **/
    private boolean logChangedMap = true;

    /** The period of time (in minutes) to hold the asynchronous tasks' statuses in the asynchronous tasks cache **/
    private long cacheTimeInMinutes;

    /**Map of tasks in DB per storage pool that exist after restart **/
    private ConcurrentMap<Guid, List<AsyncTask>> tasksInDbAfterRestart = null;

    /**
     * Map of tasks for commands that have been partially submitted to vdsm
     */
    private Map<Guid, AsyncTask> partiallyCompletedCommandTasks = new ConcurrentHashMap<>();

    private CountDownLatch irsBrokerLatch;

    @PostConstruct
    private void init() {
        cacheTimeInMinutes = Config.<Long>getValue(ConfigValues.AsyncTaskStatusCachingTimeInMinutes);
        tasks = new ConcurrentHashMap<>();

        schedulerService.scheduleWithFixedDelay(this::timerElapsed,
                Config.<Long> getValue(ConfigValues.AsyncTaskPollingRate),
                Config.<Long> getValue(ConfigValues.AsyncTaskPollingRate),
                TimeUnit.SECONDS);

        schedulerService.scheduleWithFixedDelay(this::cacheTimerElapsed,
                Config.<Long> getValue(ConfigValues.AsyncTaskStatusCacheRefreshRateInSeconds),
                Config.<Long> getValue(ConfigValues.AsyncTaskStatusCacheRefreshRateInSeconds),
                TimeUnit.SECONDS);
        initAsyncTaskManager();
    }

    // NOTE - any change to the logic here and in the async tasks managed command requires inspection of
    // CommandExecutor.handleUnmanagedCommands()
    public void initAsyncTaskManager() {
        tasksInDbAfterRestart = new ConcurrentHashMap<>();
        Map<Guid, List<AsyncTask>> rootCommandIdToTasksMap = groupTasksByRootCommandId(coco.getAllAsyncTasksFromDb());
        int numberOfCommandsPartiallyExecuted = 0;
        for (Entry<Guid, List<AsyncTask>> entry : rootCommandIdToTasksMap.entrySet()) {
            if (isPartiallyExecutedCommand(rootCommandIdToTasksMap.get(entry.getKey()))) {
                log.info("Root Command '{}' has partially executed task.", entry.getKey());
                numberOfCommandsPartiallyExecuted++;
            }
        }
        irsBrokerLatch = new CountDownLatch(numberOfCommandsPartiallyExecuted);
        for (Entry<Guid, List<AsyncTask>> entry : rootCommandIdToTasksMap.entrySet()) {
            if (isPartiallyExecutedCommand(rootCommandIdToTasksMap.get(entry.getKey()))) {
                log.info("Root Command '{}' has partially executed tasks.", entry.getKey());
                handlePartiallyExecuteTasksOfCommand(rootCommandIdToTasksMap.get(entry.getKey()));
            }
            for (AsyncTask task : entry.getValue()) {
                if (!isPartiallyExecutedTask(task)) {
                    tasksInDbAfterRestart.putIfAbsent(task.getStoragePoolId(), new ArrayList<>());
                    tasksInDbAfterRestart.get(task.getStoragePoolId()).add(task);
                }
            }
        }
        try {
            irsBrokerLatch.await();
            log.info("Initialization of AsyncTaskManager completed successfully.");
        } catch (InterruptedException ignore) {
        }

    }

    private synchronized void timerElapsed() {
        try {
            timerElapsedImpl();
        } catch (Throwable t) {
            log.error("Exception in timerElapsed: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    private synchronized void timerElapsedImpl() {
        if (thereAreTasksToPoll()) {
            pollAndUpdateAsyncTasks();

            if (thereAreTasksToPoll() && logChangedMap) {
                log.info("Finished polling Tasks, will poll again in {} seconds.",
                        Config.<Long>getValue(ConfigValues.AsyncTaskPollingRate));

                // Set indication to false for not logging the same message next
                // time.
                logChangedMap = false;
            }

            // check for zombie tasks
            if (tasks.size() > 0) {
                cleanZombieTasks();
            }
        }
    }

    private void cacheTimerElapsed() {
        try {
            removeClearedAndOldTasks();
        } catch (Throwable t) {
            log.error("Exception in cacheTimerElapsed: {}", ExceptionUtils.getRootCauseMessage(t));
            log.debug("Exception", t);
        }
    }

    /**
     * Check if task should be cached or not. Task should not be cached , only
     * if the task has been rolled back (Cleared) or failed to be rolled back
     * (ClearedFailed), and been in that status for several minutes
     * (cacheTimeInMinutes).
     *
     * @param task - Asynchronous task we check to cache or not.
     * @return - true for uncached object , and false when the object should be
     * cached.
     */
    public synchronized boolean cachingOver(SPMTask task) {
        // Get time in milliseconds that the task should be cached
        long SubtractMinutesAsMills = TimeUnit.MINUTES
                .toMillis(cacheTimeInMinutes);

        // check if task has been rolled back (Cleared) or failed to be rolled
        // back (ClearedFailed)
        // for SubtractMinutesAsMills of minutes.
        return (task.getState() == AsyncTaskState.Cleared || task.getState() == AsyncTaskState.ClearFailed)
                && task.getLastAccessToStatusSinceEnd() < (System
                .currentTimeMillis() - SubtractMinutesAsMills);
    }

    public synchronized boolean hasTasksByStoragePoolId(Guid storagePoolID) {
        boolean retVal = false;
        if (tasks != null) {
            for (SPMTask task : tasks.values()) {
                if (task.getStoragePoolID().equals(storagePoolID)) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    public synchronized boolean hasTasksForEntityIdAndAction(Guid id, ActionType type) {
        if (tasks != null) {
            for (SPMTask task : tasks.values()) {
                if (isCurrentTaskLookedFor(id, task)
                        && type.equals(task.getParameters().getDbAsyncTask().getActionType())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void handlePartiallyExecuteTasksOfCommand(final List<AsyncTask> tasks) {
        ThreadPoolUtil.execute(() -> TransactionSupport.executeInNewTransaction(() -> {
            try {
                for (AsyncTask task : tasks) {
                    handlePartiallyExecutedTaskOfCommand(task);
                }
                return null;
            } finally {
                irsBrokerLatch.countDown();
            }
        }));
    }

    private boolean isPartiallyExecutedCommand(List<AsyncTask> tasks) {
        return tasks.isEmpty() ? false : !tasks.get(0).getRootCmdEntity().isExecuted();
    }

    private static boolean hasEmptyVdsmId(AsyncTask task) {
        return Guid.Empty.equals(task.getVdsmTaskId());
    }

    /**
     * We group the tasks by the root command id, so that we can identify the
     * commands that were partially submitted to vdsm
     */
    private Map<Guid, List<AsyncTask>> groupTasksByRootCommandId(List<AsyncTask> tasksInDB) {
        return tasksInDB.stream().collect(Collectors.groupingBy(AsyncTask::getRootCommandId));
    }

    private static boolean isPartiallyExecutedTask(AsyncTask task) {
        return !task.getChildCmdEntity().isExecuted();
    }

    /**
     * If a command is partially submitted to vdsm, the empty place holders
     * can be removed from the database and we poll for the completion of tasks
     * that were already submitted. Once the tasks that were submitted finish
     * execution, they are marked to be failed by adding them to the
     * partiallyCompletedCommandTasks list.
     * If none of the tasks were submitted to vdsm, the empty place holders
     * are deleted from the database and we endAction on the command with
     * failure
     */
    private void handlePartiallyExecutedTaskOfCommand(
            final AsyncTask task) {

        if (hasEmptyVdsmId(task)) {
            removeTaskFromDbByTaskId(task.getTaskId());
            return;
        }
        partiallyCompletedCommandTasks.put(task.getVdsmTaskId(), task);
    }

    public void logAndFailTaskOfCommandWithEmptyVdsmId(Guid asyncTaskId, String message) {
        AsyncTask task = coco.getAsyncTaskFromDb(asyncTaskId);
        if (task != null) {
            logAndFailPartiallySubmittedTaskOfCommand(task, message);
        }
    }

    public void logAndFailPartiallySubmittedTaskOfCommand(final AsyncTask task, String message) {
        log.info(
                "Failing partially submitted task AsyncTaskType '{}': Task '{}' Parent Command '{}'",
                task.getTaskType(),
                task.getTaskId(),
                task.getActionType());
        task.getTaskParameters().setTaskGroupSuccess(false);
        if (task.getActionType() == ActionType.Unknown) {
            removeTaskFromDbByTaskId(task.getTaskId());
            log.info(
                    "Not calling endAction for partially submitted task and AsyncTaskType '{}': Task '{}' Parent Command '{}'",
                    task.getTaskType(),
                    task.getTaskId(),
                    task.getActionType());
            return;
        }
        log.info("Calling updateTask for partially submitted task and AsyncTaskType '{}': Task '{}' Parent Command"
                        + " '{}' Parameters class '{}'",
                task.getTaskType(),
                task.getTaskId(),
                task.getActionType());
        AsyncTaskCreationInfo creationInfo = new AsyncTaskCreationInfo(Guid.Empty, task.getTaskType(), task.getStoragePoolId());
        SPMTask spmTask = coco.construct(creationInfo, task);
        AsyncTaskStatus failureStatus = new AsyncTaskStatus();
        failureStatus.setStatus(AsyncTaskStatusEnum.finished);
        failureStatus.setResult(AsyncTaskResultEnum.failure);
        failureStatus.setMessage(message);
        spmTask.setState(AsyncTaskState.Ended);
        spmTask.setLastTaskStatus(failureStatus);
        spmTask.updateTask(failureStatus);
    }

    public void removeTaskFromDbByTaskId(Guid taskId) {
        try {
            if (coco.removeTaskFromDbByTaskId(taskId) != 0) {
                log.info("Removed task '{}' from DataBase", taskId);
            }
        } catch (RuntimeException e) {
            log.error("Removing task '{}' from DataBase threw an exception: {}",
                    taskId,
                    e.getMessage());
            log.debug("Exception", e);
        }
    }

    private boolean isCurrentTaskLookedFor(Guid id, SPMTask task) {
        return (task instanceof CommandAsyncTask) && task.getParameters().getEntityInfo() != null &&
                id.equals(task.getParameters().getEntityInfo().getId())
                && (task.getState() != AsyncTaskState.Cleared)
                && (task.getState() != AsyncTaskState.ClearFailed);
    }

    private void cleanZombieTasks() {
        long maxTime = DateTime.getNow()
                .addMinutes(-1 * Config.<Integer>getValue(ConfigValues.AsyncTaskZombieTaskLifeInMinutes)).getTime();
        int asyncZombieTaskLife = Config.<Integer> getValue(ConfigValues.AsyncTaskZombieTaskLifeInMinutes);
        for (SPMTask task : tasks.values()) {

            if (task.getParameters().getDbAsyncTask().getStartTime().getTime() < maxTime) {
                AuditLogable logable = new AuditLogableImpl();
                logable.addCustomValue("CommandName", task.getParameters().getDbAsyncTask().getActionType().toString());
                logable.addCustomValue("Date", task.getParameters().getDbAsyncTask().getStartTime().toString());

                // if task is not finish and not unknown then it's in running
                // status
                if (task.getLastTaskStatus().getStatus() != AsyncTaskStatusEnum.finished
                        && task.getLastTaskStatus().getStatus() != AsyncTaskStatusEnum.unknown) {
                    // mark it as a zombie task, Will result in failure of the command
                    task.setZombieTask(true);
                    auditLogDirector.log(logable, AuditLogType.TASK_STOPPING_ASYNC_TASK);

                    log.info(
                            "Cleaning zombie tasks: Stopping async task '{}' that started at '{}' since it reached a timeout of {} minutes",
                            task.getParameters().getDbAsyncTask().getActionType(),
                            task.getParameters().getDbAsyncTask().getStartTime(),
                            asyncZombieTaskLife);

                    task.stopTask(true);
                } else {
                    auditLogDirector.log(logable, AuditLogType.TASK_CLEARING_ASYNC_TASK);

                    log.info(
                            "Cleaning zombie tasks: Clearing async task '{}' that started at '{}' since it reached a timeout of {} minutes",
                            task.getParameters().getDbAsyncTask().getActionType(),
                            task.getParameters().getDbAsyncTask().getStartTime(),
                            asyncZombieTaskLife);

                    task.clearAsyncTask(true);
                }
            }
        }
    }

    private int numberOfTasksToPoll() {
        int retValue = 0;
        for (SPMTask task : tasks.values()) {
            if (task.getShouldPoll()) {
                retValue++;
            }
        }

        return retValue;
    }

    private boolean thereAreTasksToPoll() {
        for (SPMTask task : tasks.values()) {
            if (task.getShouldPoll()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fetch all tasks statuses from each storagePoolId , and update the tasks
     * map with the updated statuses.
     */
    private void pollAndUpdateAsyncTasks() {
        if (logChangedMap) {
            log.info("Polling and updating Async Tasks: {} tasks, {} tasks to poll now",
                    tasks.size(), numberOfTasksToPoll());
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
     * @param poolsAllTasksMap Task statuses Map fetched from VDSM.
     */
    private void updateTaskStatuses(
            Map<Guid, Map<Guid, AsyncTaskStatus>> poolsAllTasksMap) {
        for (SPMTask task : tasks.values()) {
            if (task.getShouldPoll()) {
                Map<Guid, AsyncTaskStatus> asyncTasksForPoolMap = poolsAllTasksMap
                        .get(task.getStoragePoolID());

                // If the storage pool id exists
                if (asyncTasksForPoolMap != null) {
                    AsyncTaskStatus cachedAsyncTaskStatus = asyncTasksForPoolMap
                            .get(task.getVdsmTaskId());

                    log.debug("Updating task of command {} with id '{}' to status '{}'.",
                            task.getParameters().getDbAsyncTask().getActionType(),
                            task.getCommandId(),
                            cachedAsyncTaskStatus);

                    // task found in VDSM.
                    task.updateTask(cachedAsyncTaskStatus);
                }
            } else {
                log.debug("Not updating task of command {} with id '{}' and status '{}'.",
                        task.getParameters().getDbAsyncTask().getActionType(),
                        task.getCommandId(),
                        task.getLastTaskStatus());
            }
        }

    }

    /**
     * Call VDSCommand for each pool id fetched from poolsOfActiveTasks , and
     * Initialize a map with each storage pool Id task statuses.
     *
     * @param poolsOfActiveTasks - Set of all the active tasks fetched from
     * tasks.
     * @return poolsAsyncTaskMap - Map which contains tasks for each storage
     * pool id.
     */
    private Map<Guid, Map<Guid, AsyncTaskStatus>> getSPMsTasksStatuses(Set<Guid> poolsOfActiveTasks) {
        Map<Guid, Map<Guid, AsyncTaskStatus>> poolsAsyncTaskMap = new HashMap<>();

        // For each pool Id (SPM) ,add its tasks to the map.
        for (Guid storagePoolID : poolsOfActiveTasks) {
            try {
                Map<Guid, AsyncTaskStatus> map = coco.getAllTasksStatuses(storagePoolID);
                if (map != null) {
                    poolsAsyncTaskMap.put(storagePoolID, map);
                }
            } catch (RuntimeException e) {
                if ((e instanceof EngineException)
                        && (((EngineException) e).getErrorCode() == EngineError.VDS_NETWORK_ERROR)) {
                    log.debug("Get SPM task statuses: Calling Command {}VDSCommand, "
                                    + "with storagePoolId '{}') threw an exception.",
                            VDSCommandType.SPMGetAllTasksStatuses, storagePoolID);
                } else {
                    log.debug("Get SPM task statuses: Calling Command {}VDSCommand, "
                                    + "with storagePoolId '{}') threw an exception: {}",
                            VDSCommandType.SPMGetAllTasksStatuses, storagePoolID, e.getMessage());
                }
                log.debug("Exception", e);
            }
        }

        return poolsAsyncTaskMap;
    }

    /**
     * Get a Set of all the storage pool id's of tasks that should pool.
     *
     * @return - Set of active tasks.
     * @see SPMAsyncTask#getShouldPoll()
     */
    private Set<Guid> getPoolIdsTasks() {
        Set<Guid> poolsOfActiveTasks = new HashSet<>();

        for (SPMTask task : tasks.values()) {
            if (task.getShouldPoll()) {
                poolsOfActiveTasks.add(task.getStoragePoolID());
            }
        }
        return poolsOfActiveTasks;
    }

    /**
     * get list of pools that have only cleared and old tasks (which don't exist
     * anymore in the manager):
     */
    private synchronized void removeClearedAndOldTasks() {
        Set<Guid> poolsOfActiveTasks = new HashSet<>();
        Set<Guid> poolsOfClearedAndOldTasks = new HashSet<>();
        ConcurrentMap<Guid, SPMTask> activeTaskMap = new ConcurrentHashMap<>();
        for (SPMTask task : tasks.values()) {
            if (!cachingOver(task)) {
                activeTaskMap.put(task.getVdsmTaskId(), task);
                poolsOfActiveTasks.add(task.getStoragePoolID());
            } else {
                poolsOfClearedAndOldTasks.add(task.getStoragePoolID());
            }
        }

        // Check if tasks need to be updated with less tasks (activated tasks).
        if (poolsOfClearedAndOldTasks.size() > 0) {
            setNewMap(activeTaskMap);
            poolsOfClearedAndOldTasks.removeAll(poolsOfActiveTasks);
        }
        for (Guid storagePoolID : poolsOfClearedAndOldTasks) {
            log.info("Cleared all tasks of pool '{}'.",
                    storagePoolID);
        }
    }

    public synchronized void lockAndAddTaskToManager(SPMTask task) {
        addTaskToManager(task);
    }

    private void addTaskToManager(SPMTask task) {
        if (task == null) {
            log.error("Cannot add a null task.");
        } else {
            if (!tasks.containsKey(task.getVdsmTaskId())) {
                log.info(
                        "Adding task '{}' (Parent Command '{}', Parameters Type '{}'), {}.",
                        task.getVdsmTaskId(),
                        task.getParameters().getDbAsyncTask().getActionType(),
                        task.getParameters().getClass().getName(),
                        task.getShouldPoll() ? "polling started."
                                : "polling hasn't started yet.");

                // Set the indication to true for logging tasks status on next
                // execution.
                addTaskToMap(task.getVdsmTaskId(), task);
            } else {
                SPMTask existingTask = tasks.get(task.getVdsmTaskId());
                if (existingTask.getParameters().getDbAsyncTask().getActionType() == ActionType.Unknown
                        && task.getParameters().getDbAsyncTask().getActionType() != ActionType.Unknown) {
                    log.info(
                            "Task '{}' already exists with action type 'Unknown', now overriding it with action type '{}'",
                            task.getVdsmTaskId(),
                            task.getParameters().getDbAsyncTask().getActionType());

                    // Set the indication to true for logging tasks status on
                    // next execution.
                    addTaskToMap(task.getVdsmTaskId(), task);
                }
            }
        }
    }

    /**
     * Adds new task to tasks map , and set the log status to true. We set the
     * indication to true for logging tasks status on next execution.
     *
     * @param guid - Key of the map.
     * @param asyncTask - Value of the map.
     */
    private void addTaskToMap(Guid guid, SPMTask asyncTask) {
        tasks.put(guid, asyncTask);
        logChangedMap = true;
    }

    /**
     * We set the indication to true when tasks map changes for logging tasks
     * status on next execution.
     *
     * @param asyncTaskMap - Map to copy to tasks map.
     */
    private void setNewMap(ConcurrentMap<Guid, SPMTask> asyncTaskMap) {
        // If not the same set tasks to be as asyncTaskMap.
        tasks = asyncTaskMap;

        // Set the indication to true for logging.
        logChangedMap = true;

        // Log tasks to poll now.
        log.info("Setting new tasks map. The map contains now {} tasks", tasks.size());
    }

    public SPMTask createTask(AsyncTaskType taskType, AsyncTaskParameters taskParameters) {
        return coco.construct(taskType, taskParameters, false);
    }

    public synchronized void startPollingTask(Guid vdsmTaskId) {
        if (tasks.containsKey(vdsmTaskId)) {
            tasks.get(vdsmTaskId).startPollingTask();
        }
    }

    public synchronized ArrayList<AsyncTaskStatus> pollTasks(ArrayList<Guid> vdsmTaskIdList) {
        ArrayList<AsyncTaskStatus> returnValue = new ArrayList<>();

        if (vdsmTaskIdList != null && vdsmTaskIdList.size() > 0) {
            for (Guid vdsmTaskId : vdsmTaskIdList) {
                returnValue.add(getTaskStatus(vdsmTaskId));
            }
        }

        return returnValue;
    }

    // similar to previous function but returns a map
    public Map<Guid, AsyncTaskStatus> pollTasksWithStatus(Set<Guid> taskIds) {
        if (taskIds == null) {
            return Collections.emptyMap();
        }
        return taskIds.stream().collect(Collectors.toMap(Function.identity(), this::getTaskStatus));
    }

    private AsyncTaskStatus getTaskStatus(Guid vdsmTaskId) {
        SPMTask spmTask = tasks.get(vdsmTaskId);
        if (spmTask != null) {
            // task is still running or is still in the cache:
            spmTask.setLastStatusAccessTime();
            return spmTask.getLastTaskStatus();
        } else {
            // task doesn't exist in the manager (shouldn't happen) ->
            // assume it has been ended successfully.
            log.warn("Polling tasks. Task ID '{}' doesn't exist in the manager -> assuming 'finished'.", vdsmTaskId);
            AsyncTaskStatus result = new AsyncTaskStatus();
            result.setStatus(AsyncTaskStatusEnum.finished);
            result.setResult(AsyncTaskResultEnum.success);
            return result;
        }
    }

    /**
     * Retrieves from the specified storage pool the tasks that exist on it and
     * adds them to the manager.
     *
     * @param sp the storage pool to retrieve running tasks from
     */
    public void addStoragePoolExistingTasks(StoragePool sp) {
        List<AsyncTaskCreationInfo> currPoolTasks = null;
        try {
            currPoolTasks = coco.getAllTasksInfo(sp.getId());
        } catch (RuntimeException e) {
            log.error("Getting existing tasks on Storage Pool '{}' failed: {}",
                    sp.getName(),
                    e.getMessage());
            log.debug("Exception", e);
        }

        if (currPoolTasks != null && currPoolTasks.size() > 0) {
            synchronized (this) {
                final List<SPMTask> newlyAddedTasks = new ArrayList<>();

                for (AsyncTaskCreationInfo creationInfo : currPoolTasks) {
                    creationInfo.setStoragePoolID(sp.getId());
                    if (!tasks.containsKey(creationInfo.getVdsmTaskId())) {
                        try {
                            SPMTask task;
                            if (partiallyCompletedCommandTasks.containsKey(creationInfo.getVdsmTaskId())) {
                                AsyncTask asyncTaskInDb = partiallyCompletedCommandTasks.get(creationInfo.getVdsmTaskId());
                                task = coco.construct(creationInfo, asyncTaskInDb);
                                if (task.getEntitiesMap() == null) {
                                    task.setEntitiesMap(new HashMap<>());
                                }
                                partiallyCompletedCommandTasks.remove(task.getVdsmTaskId());
                                // mark it as a task of a partially completed command
                                // Will result in failure of the command
                                task.setPartiallyCompletedCommandTask(true);
                            } else {
                                task = asyncTaskFactory.construct(creationInfo);
                            }
                            addTaskToManager(task);
                            newlyAddedTasks.add(task);
                        } catch (Exception e) {
                            log.error("Failed to load task of type '{}' with id '{}': {}.",
                                    creationInfo.getTaskType(), creationInfo.getVdsmTaskId(),
                                    ExceptionUtils.getRootCauseMessage(e));
                            log.debug("Exception", e);
                        }
                    }
                }

                TransactionSupport.executeInNewTransaction(() -> {
                    for (SPMTask task : newlyAddedTasks) {
                        asyncTaskUtils.addOrUpdateTaskInDB(coco, task);
                    }
                    return null;
                });

                for (SPMTask task : newlyAddedTasks) {
                    startPollingTask(task.getVdsmTaskId());
                }

                log.info(
                        "Discovered {} tasks on Storage Pool '{}', {} added to manager.",
                        currPoolTasks.size(),
                        sp.getName(),
                        newlyAddedTasks.size());
            }
        } else {
            log.info("Discovered no tasks on Storage Pool '{}'",
                    sp.getName());
        }


        List<AsyncTask> tasksInDForStoragePool = tasksInDbAfterRestart.get(sp.getId());
        if (tasksInDForStoragePool != null) {
            for (AsyncTask task : tasksInDForStoragePool) {
                if (!tasks.containsKey(task.getVdsmTaskId())) {
                    coco.removeByVdsmTaskId(task.getVdsmTaskId());
                }
            }
        }

        //Either the tasks were only in DB - so they were removed from db, or they are polled -
        //in any case no need to hold them in the map that represents the tasksInDbAfterRestart
        tasksInDbAfterRestart.remove(sp.getId());

    }

    /**
     * Stops all tasks, and set them to polling state, for clearing them up
     * later.
     *
     * @param vdsmTaskList - List of tasks to stop.
     */
    public synchronized void cancelTasks(List<Guid> vdsmTaskList) {
        for (Guid vdsmTaskId : vdsmTaskList) {
            cancelTask(vdsmTaskId);
        }
    }

    public synchronized void cancelTask(Guid vdsmTaskId) {
        if (tasks.containsKey(vdsmTaskId)) {
            log.info("Attempting to cancel task '{}'.", vdsmTaskId);
            tasks.get(vdsmTaskId).stopTask();
            tasks.get(vdsmTaskId).concreteStartPollingTask();
        }
    }

    public synchronized boolean entityHasTasks(Guid id) {
        for (SPMTask task : tasks.values()) {
            if (isCurrentTaskLookedFor(id, task)) {
                return true;
            }
        }
        return false;
    }

    public Collection<Guid> getUserIdsForVdsmTaskIds(List<Guid> vdsmTaskIds) {
        Set<Guid> users = new TreeSet<>();
        for (Guid id : vdsmTaskIds) {
            if (tasks.containsKey(id)) {
                users.add(tasks.get(id).getParameters().getDbAsyncTask().getUserId());
            }
        }
        return users;
    }

    public boolean doesCommandContainAsyncTask(Guid cmdId) {
        for (SPMTask task : tasks.values()) {
            if (task.getParameters().getDbAsyncTask().getCommandId().equals(cmdId)) {
                return true;
            }
        }
        return false;
    }

}
