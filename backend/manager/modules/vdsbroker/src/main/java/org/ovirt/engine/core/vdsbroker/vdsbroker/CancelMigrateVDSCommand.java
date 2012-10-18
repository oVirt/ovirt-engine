package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class CancelMigrateVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<P> {
    public CancelMigrateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        Guid vmId = getParameters().getVmId();
        status = getBroker().migrateCancel(vmId.toString());
        ProceedProxyReturnValue();
        ResourceManager.getInstance().RemoveAsyncRunningVm(vmId);
    }
}
