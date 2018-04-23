package org.ovirt.engine.core.bll.tasks;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public final class AsyncTaskFactory {

    @Inject
    private Instance<CommandCoordinator> cocoInstance;

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskFactory.class);
    /**
     * Constructs a task based on creation info (task type and task parameters
     * as retrieved from the vdsm). Use in order to construct tasks when service
     * is initializing.
     * @param creationInfo
     *          The Asyc Task Creation info
     */
    public SPMAsyncTask construct(AsyncTaskCreationInfo creationInfo) {
        CommandCoordinator coco = cocoInstance.get();
        AsyncTask asyncTask = coco.getByVdsmTaskId(creationInfo.getVdsmTaskId());
        if (asyncTask == null || asyncTask.getActionParameters() == null) {
            asyncTask = new AsyncTask(AsyncTaskResultEnum.success,
                    AsyncTaskStatusEnum.running,
                    Guid.Empty,
                    creationInfo.getVdsmTaskId(),
                    creationInfo.getStepId(),
                    creationInfo.getStoragePoolID(),
                    creationInfo.getTaskType(),
                    getCommandEntity(coco, asyncTask == null ? Guid.newGuid() : asyncTask.getRootCommandId()),
                    getCommandEntity(coco, asyncTask == null ? Guid.newGuid() : asyncTask.getCommandId()));
            creationInfo.setTaskType(AsyncTaskType.unknown);
        }
        AsyncTaskParameters asyncTaskParams = new AsyncTaskParameters(creationInfo, asyncTask);
        return construct(creationInfo.getTaskType(), asyncTaskParams, true);
    }

    private CommandEntity getCommandEntity(CommandCoordinator coco, Guid cmdId) {
        CommandEntity cmdEntity = coco.getCommandEntity(cmdId);
        if (cmdEntity == null) {
            cmdEntity = coco.createCommandEntity(cmdId, ActionType.Unknown, new ActionParametersBase());
        }
        return cmdEntity;
    }

    /**
     * Constructs a task based on its type and the task type's parameters.
     *
     * @param taskType
     *            the type of the task which we want to construct.
     * @param asyncTaskParams
     *            the parameters by which we construct the task.
     * @param duringInit
     *            If this method is called during initialization
     */
    public SPMAsyncTask construct(AsyncTaskType taskType,
            AsyncTaskParameters asyncTaskParams,
            boolean duringInit) {
        try {
            SPMAsyncTask result = null;
            if (taskType == AsyncTaskType.unknown ||
                    asyncTaskParams.getDbAsyncTask().getActionType() == ActionType.Unknown) {
                result = new SPMAsyncTask(cocoInstance.get(), asyncTaskParams);
            } else {
                result = new CommandAsyncTask(cocoInstance.get(), asyncTaskParams, duringInit);
            }
            return result;
        } catch (Exception e) {
            log.error("AsyncTaskFactory: Failed to get type information using reflection for AsyncTask type '{}': {}",
                    taskType,
                    e.getMessage());
            log.debug("Exception", e);
            return null;
        }
    }
}
