package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.LeaseVDSParameters;

public class HotUnplugLeaseVDSCommand<P extends LeaseVDSParameters> extends VdsBrokerCommand<P> {

    public HotUnplugLeaseVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().hotunplugLease(getParameters().getVmId(), getParameters().getStorageDomainId());
        proceedProxyReturnValue();
    }
}
