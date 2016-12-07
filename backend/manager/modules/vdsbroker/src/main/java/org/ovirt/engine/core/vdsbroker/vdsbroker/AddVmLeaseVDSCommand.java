package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmLeaseVDSParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;;

public class AddVmLeaseVDSCommand<T extends VmLeaseVDSParameters> extends IrsBrokerCommand<T> {

    public AddVmLeaseVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        status = getIrsProxy().addVmLease(
                getParameters().getLeaseId().toString(),
                getParameters().getStorageDomainId().toString());
        proceedProxyReturnValue();
    }

}
