package org.ovirt.engine.core.bll.executor;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;

public interface BackendActionExecutor {
    ActionReturnValue execute(CommandBase<?> command);
}
