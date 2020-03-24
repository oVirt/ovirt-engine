package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmBackupVDSParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmBackupInfo;

public class GetVmBackupInfoVDSCommand<P extends VmBackupVDSParameters> extends VdsBrokerCommand<P> {

    private VmBackupInfo vmBackupInfo;

    public GetVmBackupInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmBackupInfo = getBroker().vmBackupInfo(
                getParameters().getVmBackup().getVmId().toString(),
                getParameters().getVmBackup().getId().toString(),
                getParameters().getVmBackup().getToCheckpointId().toString());
        proceedProxyReturnValue();
        setReturnValue(vmBackupInfo);
    }


    @Override
    protected Object getReturnValueFromBroker() {
        return vmBackupInfo;
    }

    @Override
    protected Status getReturnStatus() {
        return vmBackupInfo.getStatus();
    }
}
