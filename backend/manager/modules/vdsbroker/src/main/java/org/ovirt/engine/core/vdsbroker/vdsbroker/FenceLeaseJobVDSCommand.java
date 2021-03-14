package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.FenceLeaseJobVDSParameters;

public class FenceLeaseJobVDSCommand <T extends FenceLeaseJobVDSParameters> extends VdsBrokerCommand<T> {
    private StatusOnlyReturn returnValue;

    public FenceLeaseJobVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        returnValue = getBroker().fenceLeaseJob(
                getParameters().getLeaseId().toString(),
                getParameters().getStorageDomainId().toString(),
                getParameters().getMetadata());

        proceedProxyReturnValue();
    }

    @Override
    protected Status getReturnStatus() {
        return returnValue.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return returnValue;
    }
}
