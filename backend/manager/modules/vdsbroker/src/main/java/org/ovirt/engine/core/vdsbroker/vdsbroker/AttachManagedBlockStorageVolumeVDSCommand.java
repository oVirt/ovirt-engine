package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.AttachManagedBlockStorageVolumeVDSCommandParameters;

public class AttachManagedBlockStorageVolumeVDSCommand<P extends AttachManagedBlockStorageVolumeVDSCommandParameters> extends VdsBrokerCommand<P>{

    public AttachManagedBlockStorageVolumeVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        DeviceInfoReturn deviceInfoReturn =
                getBroker().attachManagedBlockStorageVolume(getParameters().getVolumeId(), getParameters().getConnectionInfo());
        setReturnValue(deviceInfoReturn.getDeviceInfo());
    }

}
