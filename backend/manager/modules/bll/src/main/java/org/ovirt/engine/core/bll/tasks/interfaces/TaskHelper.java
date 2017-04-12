package org.ovirt.engine.core.bll.tasks.interfaces;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTask;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;

/**
 * This interfaces bridges the gap between the bll and the taskmgr packages
 * There is still a lot of dependency between the packages and as the modules
 * are refactored some of the methods could be removed.
 */
public interface TaskHelper {

    ArrayList<AsyncTaskCreationInfo> getAllTasksInfo(Guid storagePoolID);

    Map<Guid, AsyncTaskStatus> getAllTasksStatuses(Guid storagePoolID);

    void stopTask(Guid storagePoolID, Guid vdsmTaskID);

    VDSReturnValue clearTask(Guid storagePoolID, Guid vdsmTaskID);

    Guid createTask(
            Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand,
            String description,
            Map<Guid, VdcObjectType> entitiesMap);

    SPMAsyncTask concreteCreateTask(
            Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand);

    void cancelTasks(CommandBase<?> command);

    void revertTasks(CommandBase<?> command);

    AsyncTask getAsyncTask(
            Guid taskId,
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand);

    AsyncTask createAsyncTask(
            CommandBase<?> command,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            ActionType parentCommand);

    SPMTask construct(AsyncTaskCreationInfo creationInfo);

    SPMTask construct(AsyncTaskCreationInfo creationInfo, AsyncTask asyncTask);

    SPMTask construct(AsyncTaskType taskType,
            AsyncTaskParameters asyncTaskParams,
            boolean duringInit);

    boolean doesCommandContainAsyncTask(Guid cmdId);
}
