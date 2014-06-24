package org.ovirt.engine.core.bll.tasks;

import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTasks;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public final class AsyncTaskFactory {
    /**
     * Constructs a task based on creation info (task type and task parameters
     * as retrieved from the vdsm). Use in order to construct tasks when service
     * is initializing.
     * @param coco
     *          Handle to command coordinator
     * @param creationInfo
     *          The Asyc Task Creation info
     * @return
     */
    public static SPMAsyncTask construct(CommandCoordinator coco, AsyncTaskCreationInfo creationInfo) {
        AsyncTasks asyncTask = coco.getByVdsmTaskId(creationInfo.getVdsmTaskId());
        if (asyncTask == null || asyncTask.getActionParameters() == null) {
            CommandStatus cmdStatus = asyncTask == null ? CommandStatus.UNKNOWN : asyncTask.getCommandStatus();
            asyncTask =
                    new AsyncTasks(VdcActionType.Unknown,
                            AsyncTaskResultEnum.success,
                            AsyncTaskStatusEnum.running,
                            creationInfo.getVdsmTaskId(),
                            new VdcActionParametersBase(),
                            new VdcActionParametersBase(),
                            creationInfo.getStepId(),
                            asyncTask == null ? Guid.newGuid() : asyncTask.getCommandId(),
                            asyncTask == null ? Guid.newGuid() : asyncTask.getRootCommandId(),
                            creationInfo.getStoragePoolID(),
                            creationInfo.getTaskType(),
                            cmdStatus);
            creationInfo.setTaskType(AsyncTaskType.unknown);
        }
        AsyncTaskParameters asyncTaskParams = new AsyncTaskParameters(creationInfo, asyncTask);
        return construct(coco, creationInfo.getTaskType(), asyncTaskParams, true);
    }

    public static SPMAsyncTask construct(CommandCoordinator coco, AsyncTaskCreationInfo creationInfo, AsyncTasks asyncTask) {
        AsyncTaskParameters asyncTaskParams = new AsyncTaskParameters(creationInfo, asyncTask);
        return construct(coco, creationInfo.getTaskType(), asyncTaskParams, true);
    }

    /**
     * Constructs a task based on its type and the task type's parameters.
     *
     * @param coco
     *            handle to command coordinator
     * @param taskType
     *            the type of the task which we want to construct.
     * @param asyncTaskParams
     *            the parameters by which we construct the task.
     * @param duringInit
     *            If this method is called during initialization
     * @return
     */
    public static SPMAsyncTask construct(CommandCoordinator coco, AsyncTaskType taskType, AsyncTaskParameters asyncTaskParams, boolean duringInit) {
        try {
            SPMAsyncTask result = null;
            if (taskType == AsyncTaskType.unknown ||
                    asyncTaskParams.getDbAsyncTask().getaction_type() == VdcActionType.Unknown) {
                result = new SPMAsyncTask(coco, asyncTaskParams);
            } else {
                result = new CommandAsyncTask(coco, asyncTaskParams, duringInit);
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
