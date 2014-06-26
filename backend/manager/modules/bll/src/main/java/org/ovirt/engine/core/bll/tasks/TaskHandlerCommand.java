package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.compat.Guid;

public interface TaskHandlerCommand<T extends VdcActionParametersBase> {

    T getParameters();

    VdcActionType getActionType();

    VdcReturnValueBase getReturnValue();

    ExecutionContext getExecutionContext();

    void preventRollback();

    void setExecutionContext(ExecutionContext executionContext);

    Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds);

    Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand);

    Guid persistAsyncTaskPlaceHolder();

    Guid persistAsyncTaskPlaceHolder(String taskKey);

    Guid getAsyncTaskId(String key);

    Guid getAsyncTaskId();

    ArrayList<Guid> getTaskIdList();

    void deleteAsyncTaskPlaceHolder(String taskKey);

    CompensationContext getCompensationContext();

    CommandContext getContext();

    CommandContext cloneContextAndDetachFromParent();

    void taskEndSuccessfully();
}
