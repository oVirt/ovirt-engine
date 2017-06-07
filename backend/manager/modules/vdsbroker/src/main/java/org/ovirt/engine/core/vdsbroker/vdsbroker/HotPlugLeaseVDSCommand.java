package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.LeaseVDSParameters;

public class HotPlugLeaseVDSCommand<P extends LeaseVDSParameters> extends VdsBrokerCommand<P> {

    public HotPlugLeaseVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().hotplugLease(getParameters().getVmId(), getParameters().getStorageDomainId());
        proceedProxyReturnValue();
    }
}
