package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.BondParametersBase;

public abstract class VdsBondCommand<T extends BondParametersBase> extends VdsCommand<T> {

    public VdsBondCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public String getBondName() {
        return getParameters().getBondName();
    }
}
