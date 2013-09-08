package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;

public class HotUnPlugDiskVDSCommand<P extends HotPlugDiskVDSParameters> extends HotPlugDiskVDSCommand<P> {

    public HotUnPlugDiskVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        buildSendDataToVdsm();
        status = getBroker().hotunplugDisk(sendInfo);
        proceedProxyReturnValue();
    }

}
