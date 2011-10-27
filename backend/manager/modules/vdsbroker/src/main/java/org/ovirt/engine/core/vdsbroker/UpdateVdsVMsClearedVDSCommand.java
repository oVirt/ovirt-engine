package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class UpdateVdsVMsClearedVDSCommand<P extends UpdateVdsVMsClearedVDSCommandParameters>
        extends VdsIdVDSCommandBase<P> {
    public UpdateVdsVMsClearedVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {
        if (_vdsManager != null) {
            getVds().setvm_count(0);
            getVds().setvms_cores_count(0);
            getVds().setvm_active(0);
            getVds().setvm_migrating(0);
            _vdsManager.UpdateDynamicData(getVds().getDynamicData());
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }
}
