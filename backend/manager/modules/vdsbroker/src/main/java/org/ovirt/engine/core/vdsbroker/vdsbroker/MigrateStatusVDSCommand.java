package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.MigrateStatusVDSCommandParameters;

public class MigrateStatusVDSCommand<P extends MigrateStatusVDSCommandParameters> extends VdsBrokerCommand<P> {
    private Status status;
    public MigrateStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        MigrateStatusReturn retVal = getBroker().migrateStatus(getParameters().getVmId().toString());
        status = retVal.getStatus();
        setReturnValue(retVal);
        proceedProxyReturnValue();
    }

    @Override
    protected Status getReturnStatus() {
        return status;
    }
}
