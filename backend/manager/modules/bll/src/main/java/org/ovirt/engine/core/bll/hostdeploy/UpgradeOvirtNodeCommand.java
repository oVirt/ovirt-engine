package org.ovirt.engine.core.bll.hostdeploy;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.hostdeploy.UpdateVdsActionParameters;
import org.ovirt.engine.core.compat.Guid;

public class UpgradeOvirtNodeCommand<T extends UpdateVdsActionParameters> extends UpdateVdsCommand<T> {

    public UpgradeOvirtNodeCommand(T parameters) {
        super(parameters, VdcActionType.UpgradeOvirtNodeInternal);
    }

    protected UpgradeOvirtNodeCommand(Guid commandId) {
        super(commandId, VdcActionType.UpgradeOvirtNodeInternal);
    }
}
