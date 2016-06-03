package org.ovirt.engine.core.bll.executor;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public interface BackendActionExecutor {
    VdcReturnValueBase execute(CommandBase<?> command);
}
