package org.ovirt.engine.core.bll.tasks;

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

    Guid createTask(AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds);

}
