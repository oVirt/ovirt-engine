package org.ovirt.engine.core.bll.hostdeploy;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.compat.Guid;

public class UpgradeOvirtNodeCommand<T extends UpdateVdsActionParameters> extends UpdateVdsCommand<T> {

    public UpgradeOvirtNodeCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext, ActionType.UpgradeOvirtNodeInternal);
    }

    public UpgradeOvirtNodeCommand(Guid commandId) {
        super(commandId, ActionType.UpgradeOvirtNodeInternal);
    }
}
