package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.LeaseStatus;
import org.ovirt.engine.core.common.vdscommands.GetLeaseStatusVDSCommandParameters;

public class GetLeaseStatusVDSCommand<P extends GetLeaseStatusVDSCommandParameters> extends VdsBrokerCommand<P> {
    @Inject
    private VdsBrokerObjectsBuilder vdsBrokerObjectsBuilder;

    private VDSInfoReturn result;

    public GetLeaseStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().getLeaseStatus(getParameters().getLeaseId().toString(),
                getParameters().getStorageDomainId().toString());
        proceedProxyReturnValue();
        LeaseStatus leaseStatus = vdsBrokerObjectsBuilder.buildLeaseStatus(result.info);
        setReturnValue(leaseStatus);
    }

    @Override
    protected Status getReturnStatus() {
        return result.status;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
