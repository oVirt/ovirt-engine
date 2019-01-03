package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmBackupVDSParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.GetDisksListReturn;

public class GetVmBackupInfoVDSCommand<P extends VmBackupVDSParameters> extends VdsBrokerCommand<P> {

    private GetDisksListReturn disksListReturn;

    public GetVmBackupInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        disksListReturn = getBroker().vmBackupInfo(
                getParameters().getVmBackup().getVmId().toString(),
                getParameters().getVmBackup().getId().toString());
        proceedProxyReturnValue();
        setReturnValue(disksListReturn.getDisks());
    }


    @Override
    protected Object getReturnValueFromBroker() {
        return disksListReturn;
    }

    @Override
    protected Status getReturnStatus() {
        return disksListReturn.getStatus();
    }
}
