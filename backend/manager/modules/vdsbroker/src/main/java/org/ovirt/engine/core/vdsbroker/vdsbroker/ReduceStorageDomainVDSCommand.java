package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.ReduceStorageDomainVDSCommandParameters;

public class ReduceStorageDomainVDSCommand<P extends ReduceStorageDomainVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public ReduceStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().reduceDomain(getParameters().getJobId().toString(), buildReduceParams());

        proceedProxyReturnValue();
    }

    private Map<String, Object> buildReduceParams() {
        Map<String, Object> info = new HashMap<>();
        info.put("sd_id", getParameters().getStorageDomainId().toString());
        info.put("guid", getParameters().getDeviceId().toString());
        return info;
    }
}
