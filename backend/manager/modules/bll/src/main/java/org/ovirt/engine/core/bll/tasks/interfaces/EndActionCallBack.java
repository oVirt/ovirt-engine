package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.AsyncTasks;
import org.ovirt.engine.core.compat.Guid;

public interface EndActionCallBack {
    public VdcReturnValueBase endAction(Guid stepId, VdcActionType actionType, AsyncTasks dbAsyncTask, ExecutionContext context);
    public VdcReturnValueBase endAction(SPMTask task, ExecutionContext context);
}
