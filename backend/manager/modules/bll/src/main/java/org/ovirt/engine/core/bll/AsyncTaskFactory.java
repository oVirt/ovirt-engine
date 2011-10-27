package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.core.common.asynctasks.*;
import org.ovirt.engine.core.dal.dbbroker.*;

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
        async_tasks asyncTask = DbFacade.getInstance().getAsyncTaskDAO().get(creationInfo.getTaskID());
        if (asyncTask == null || asyncTask.getaction_parameters() == null) {
            asyncTask = new async_tasks(VdcActionType.Unknown, AsyncTaskResultEnum.success,
                    AsyncTaskStatusEnum.running, creationInfo.getTaskID(), new VdcActionParametersBase());
            creationInfo.setTaskType(AsyncTaskType.unknown);
        }
        AsyncTaskParameters asyncTaskParams = new AsyncTaskParameters(creationInfo, asyncTask);
        return Construct(creationInfo.getTaskType(), asyncTaskParams);
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
    public static SPMAsyncTask Construct(AsyncTaskType taskType, AsyncTaskParameters asyncTaskParams) {
        try {
            SPMAsyncTask result = null;
            if (taskType == AsyncTaskType.unknown) {
                result = new SPMAsyncTask(asyncTaskParams);
            } else {
                result = new EntityAsyncTask(asyncTaskParams);
            }
            return result;
            // java.lang.Class type =
            // java.lang.Class.forName(GetAsyncTaskTypeName(taskType));
            //
            // java.lang.Class[] types = new java.lang.Class[2];
            // types[0] =
            // BaseAsyncTaskParameters.class;//asyncTaskParams.getClass();
            // types[1] = boolean.class;
            //
            // java.lang.reflect.Constructor info = type.getConstructor(types);
            //
            // Object[] taskCtorParams = new Object[2];
            // taskCtorParams[0] = asyncTaskParams;
            // taskCtorParams[1] = pollingEnabled;
            //
            // Object TempAsCast = info.newInstance(taskCtorParams);
            // return (BaseAsyncTask)((TempAsCast instanceof BaseAsyncTask) ?
            // TempAsCast : null);
        }

        catch (Exception e) {
            log.error(String.format(
                    "AsyncTaskFactory: Failed to get type information using reflection for AsyncTask type: %1$s.",
                    taskType), e);
            return null;
        }
    }

    // private static String GetAsyncTaskTypeName(AsyncTaskType taskType)
    // {
    // return String.format("%1$s.%2$s%3$s",
    // AsyncTaskClassContainerAssemblyName, (taskType == AsyncTaskType.unknown ?
    // SpmAsyncTaskPrefix : EntityAsyncTaskPrefix), AsyncTaskClassPostfix);
    // }
    private static LogCompat log = LogFactoryCompat.getLog(AsyncTaskFactory.class);

}
