package org.ovirt.engine.core.bll.executor;

import javax.enterprise.inject.Alternative;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

@Alternative
public class DefaultBackendActionExecutor implements BackendActionExecutor {

    @Override
    public VdcReturnValueBase execute(final CommandBase<?> command) {
        return command.executeAction();
    }

}
