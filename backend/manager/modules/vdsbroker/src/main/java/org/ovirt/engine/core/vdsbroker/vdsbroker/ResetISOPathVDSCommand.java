package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.IrsBaseVDSCommandParameters;
import org.ovirt.engine.core.vdsbroker.VDSCommandBase;

public class ResetISOPathVDSCommand<T extends IrsBaseVDSCommandParameters> extends VDSCommandBase<T> {

    public ResetISOPathVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVDSCommand() {
        IsoPrefixVDSCommand.clearCachedIsoPrefix(getParameters().getStoragePoolId());
    }
}
