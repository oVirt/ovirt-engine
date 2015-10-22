package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.RefreshVolumeVDSCommandParameters;

public class RefreshVolumeVDSCommand<P extends RefreshVolumeVDSCommandParameters> extends VdsBrokerCommand<P> {

    public RefreshVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        RefreshVolumeVDSCommandParameters params = getParameters();
        status = getBroker().refreshVolume(
                params.getStorageDomainId().toString(),
                params.getStoragePoolId().toString(),
                params.getImageGroupId().toString(),
                params.getImageId().toString());
        proceedProxyReturnValue();
    }
}

