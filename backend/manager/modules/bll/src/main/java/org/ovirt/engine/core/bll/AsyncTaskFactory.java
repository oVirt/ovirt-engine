package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTasks;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public final class AsyncTaskFactory {
    /**
     * Constructs a task based on creation info (task type and task parameters
     * as retrieved from the vdsm). Use in order to construct tasks when service
     * is initializing.
     *
     * @param taskID
     *            the ID of the task to construct.
     * @param pollingEnabled
     *            true if we want to start polling the task, false otherwise.
     * @param creationInfo
     *            the info by which we construct the task.
     * @return
     */
    public static SPMAsyncTask Construct(AsyncTaskCreationInfo creationInfo) {
        AsyncTasks asyncTask = DbFacade.getInstance().getAsyncTaskDao().getByVdsmTaskId(creationInfo.getVdsmTaskId());
        if (asyncTask == null || asyncTask.getActionParameters() == null) {
            asyncTask =
                    new AsyncTasks(VdcActionType.Unknown,
                            AsyncTaskResultEnum.success,
                            AsyncTaskStatusEnum.running,
                            creationInfo.getVdsmTaskId(),
                            new VdcActionParametersBase(),
                            new VdcActionParametersBase(),
                            creationInfo.getStepId(),
                            asyncTask == null ? Guid.newGuid() : asyncTask.getCommandId(),
                            creationInfo.getStoragePoolID(),
                            creationInfo.getTaskType());
            creationInfo.setTaskType(AsyncTaskType.unknown);
        }
        AsyncTaskParameters asyncTaskParams = new AsyncTaskParameters(creationInfo, asyncTask);
        return Construct(creationInfo.getTaskType(), asyncTaskParams, true);
    }

    /**
     * Constructs a task based on its type and the task type's parameters.
     *
     * @param taskID
     *            the ID of the task to construct.
     * @param pollingEnabled
     *            true if we want to start polling the task, false otherwise.
     * @param taskType
     *            the type of the task which we want to construct.
     * @param asyncTaskParams
     *            the parameters by which we construct the task.
     * @return
     */
    public static SPMAsyncTask Construct(AsyncTaskType taskType, AsyncTaskParameters asyncTaskParams, boolean duringInit) {
        try {
            SPMAsyncTask result = null;
            if (taskType == AsyncTaskType.unknown) {
                result = new SPMAsyncTask(asyncTaskParams);
            } else {
                result = new EntityAsyncTask(asyncTaskParams, duringInit);
            }
            return result;
        }

        catch (Exception e) {
            log.error(String.format(
                    "AsyncTaskFactory: Failed to get type information using reflection for AsyncTask type: %1$s.",
                    taskType), e);
            return null;
        }
    }

    private static final Log log = LogFactory.getLog(AsyncTaskFactory.class);
}
