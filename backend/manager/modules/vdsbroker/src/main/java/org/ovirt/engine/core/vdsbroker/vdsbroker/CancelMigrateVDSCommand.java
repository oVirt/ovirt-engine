package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

public class CancelMigrateVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<P> {
    public CancelMigrateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().migrateCancel(getParameters().getVmId().toString());
        ProceedProxyReturnValue();
    }
}
