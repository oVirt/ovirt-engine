package org.ovirt.engine.core.bll.executor;

import javax.enterprise.inject.Alternative;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;

@Alternative
public class DefaultBackendActionExecutor implements BackendActionExecutor {

    @Override
    public ActionReturnValue execute(final CommandBase<?> command) {
        return command.executeAction();
    }

}
