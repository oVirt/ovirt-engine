package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.UpdateVdsVMsClearedVDSCommandParameters;

public class UpdateVdsVMsClearedVDSCommand<P extends UpdateVdsVMsClearedVDSCommandParameters>
        extends VdsIdVDSCommandBase<P> {
    public UpdateVdsVMsClearedVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {
        if (_vdsManager != null) {
            getVds().setVmCount(0);
            getVds().setVmsCoresCount(0);
            getVds().setVmActive(0);
            getVds().setVmMigrating(0);
            _vdsManager.UpdateDynamicData(getVds().getDynamicData());
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }
}
