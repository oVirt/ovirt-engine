package org.ovirt.engine.core.bll.hostdeploy;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.compat.Guid;

public class InstallVdsCommand<T extends UpdateVdsActionParameters> extends UpdateVdsCommand<T> {

    public InstallVdsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext, ActionType.InstallVdsInternal);
    }

    public InstallVdsCommand(Guid commandId) {
        super(commandId, ActionType.InstallVdsInternal);
    }

}
