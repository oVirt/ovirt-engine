package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.AttachManagedBlockStorageVolumeVDSCommandParameters;

public class DetachManagedBlockStorageVolumeVDSCommand<P extends AttachManagedBlockStorageVolumeVDSCommandParameters> extends VdsBrokerCommand<P>{

    public DetachManagedBlockStorageVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().detachManagedBlockStorageVolume(getParameters().getVolumeId());
        proceedProxyReturnValue();
        setReturnValue(status);
    }
}
