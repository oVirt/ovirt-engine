package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.MigrateStatusVDSCommandParameters;

public class MigrateStatusVDSCommand<P extends MigrateStatusVDSCommandParameters> extends VdsBrokerCommand<P> {
    private StatusForXmlRpc status;
    public MigrateStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        MigrateStatusReturnForXmlRpc retVal = getBroker().migrateStatus(getParameters().getVmId().toString());
        status = retVal.getStatus();
        setReturnValue(retVal.getDowntime());
        proceedProxyReturnValue();
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return status;
    }
}
