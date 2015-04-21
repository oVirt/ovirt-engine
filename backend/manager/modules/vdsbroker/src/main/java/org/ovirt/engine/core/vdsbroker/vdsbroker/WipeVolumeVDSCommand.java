package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.WipeVolumeVDSCommandParameters;

public class WipeVolumeVDSCommand <P extends WipeVolumeVDSCommandParameters> extends VdsBrokerCommand<P> {

    public WipeVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        log.info("-- executeVdsBrokerCommand: calling 'wipeVolume'");

        status = getBroker().wipeVolume(getParameters().getStorageDomainId().toString(), getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString());

        proceedProxyReturnValue();
    }
}
