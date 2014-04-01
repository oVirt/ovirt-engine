package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public interface EndActionCallBack {
    public VdcReturnValueBase endAction(SPMTask task, ExecutionContext context);
}
