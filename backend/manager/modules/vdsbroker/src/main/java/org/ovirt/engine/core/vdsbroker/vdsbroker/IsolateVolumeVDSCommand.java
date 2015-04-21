package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.IsolateVolumeVDSCommandParameters;

public class IsolateVolumeVDSCommand<P extends IsolateVolumeVDSCommandParameters> extends StorageDomainMetadataCommand<P> {

    public IsolateVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeDomainCommand() {
        log.info("-- executeVdsBrokerCommand: calling 'isolateVolume'");

        status = getBroker().isolateVolume(getParameters().getStorageDomainId().toString(), getParameters().getSourceImageGroupId().toString(),
                getParameters().getDestImageGroupId().toString(), getParameters().getImage().toString());

        proceedProxyReturnValue();
    }
}
