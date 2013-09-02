package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.MigrateStatusVDSCommandParameters;

public class MigrateStatusVDSCommand<P extends MigrateStatusVDSCommandParameters> extends VdsBrokerCommand<P> {
    public MigrateStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().migrateStatus(getParameters().getVmId().toString());
        proceedProxyReturnValue();
    }
}
