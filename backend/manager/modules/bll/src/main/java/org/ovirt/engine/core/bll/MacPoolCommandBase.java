package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.MacPoolDao;

public abstract class MacPoolCommandBase<T extends ActionParametersBase> extends CommandBase<T> {
    @Inject
    protected MacPoolPerCluster macPoolPerCluster;

    @Inject
    protected MacPoolDao macPoolDao;

    public MacPoolCommandBase(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__TYPE__MAC__POOL);
    }
}
