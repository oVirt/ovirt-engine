package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.AttachManagedBlockStorageVolumeVDSCommandParameters;

public class AttachManagedBlockStorageVolumeVDSCommand<P extends AttachManagedBlockStorageVolumeVDSCommandParameters> extends VdsBrokerCommand<P>{

    private DeviceInfoReturn deviceInfoReturn;

    public AttachManagedBlockStorageVolumeVDSCommand(P parameters) {
        super(parameters, parameters.getVds());
    }

    @Override
    protected void executeVdsBrokerCommand() {
        deviceInfoReturn =
                getBroker().attachManagedBlockStorageVolume(getParameters().getVolumeId(), getParameters().getConnectionInfo());
        proceedProxyReturnValue();
        setReturnValue(deviceInfoReturn.getDeviceInfo());
    }

    @Override
    protected Status getReturnStatus() {
        return deviceInfoReturn.getStatus();
    }
}
