package org.ovirt.engine.core.bll.hostdeploy;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.compat.Guid;

public class InstallVdsCommand<T extends UpdateVdsActionParameters> extends UpdateVdsCommand<T> {

    public InstallVdsCommand(T parameters) {
        super(parameters, VdcActionType.InstallVdsInternal);
    }

    protected InstallVdsCommand(Guid commandId) {
        super(commandId, VdcActionType.InstallVdsInternal);
    }

}
