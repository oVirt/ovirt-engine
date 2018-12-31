package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmBackupVDSParameters;

public class StopVmBackupVDSCommand<P extends VmBackupVDSParameters> extends VdsBrokerCommand<P> {

    public StopVmBackupVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().stopVmBackup(
                getParameters().getVmBackup().getVmId().toString(),
                getParameters().getVmBackup().getId().toString());
        proceedProxyReturnValue();
    }
}
