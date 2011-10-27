package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class ValidateStorageDomainVDSCommand<P extends ValidateStorageDomainVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public ValidateStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().validateStorageDomain(getParameters().getStorageDomainId().toString());
        ProceedProxyReturnValue();
    }
}
