package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmLeaseVDSParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;

public class RemoveVmLeaseVDSCommand<T extends VmLeaseVDSParameters> extends IrsBrokerCommand<T> {

    public RemoveVmLeaseVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        status = getIrsProxy().removeVmLease(
                getParameters().getLeaseId().toString(),
                getParameters().getStorageDomainId().toString());
        proceedProxyReturnValue();
    }

}
