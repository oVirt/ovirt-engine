package org.ovirt.engine.core.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class UpdateVdsDynamicDataVDSCommand<P extends UpdateVdsDynamicDataVDSCommandParameters>
        extends VdsIdVDSCommandBase<P> {
    public UpdateVdsDynamicDataVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsIdCommand() {
        if (_vdsManager != null) {
            _vdsManager.UpdateDynamicData(getParameters().getVdsDynamic());
        } else {
            getVDSReturnValue().setSucceeded(false);
        }
    }
}
