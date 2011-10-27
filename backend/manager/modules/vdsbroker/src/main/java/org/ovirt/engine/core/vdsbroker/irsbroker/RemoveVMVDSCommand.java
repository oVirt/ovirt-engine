package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.vdscommands.*;

public class RemoveVMVDSCommand<P extends RemoveVMVDSCommandParameters> extends IrsBrokerCommand<P> {
    public RemoveVMVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        if (getParameters().getStorageDomainId().equals(Guid.Empty)) {
            status = getIrsProxy().removeVM(getParameters().getStoragePoolId().toString(),
                    getParameters().getVmGuid().toString());
        } else {
            status = getIrsProxy()
                    .removeVM(getParameters().getStoragePoolId().toString(),
                            getParameters().getVmGuid().toString(),
                            getParameters().getStorageDomainId().toString());
        }
        ProceedProxyReturnValue();
    }
}
