package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.ChangeDiskVDSCommandParameters;

public class ChangeFloppyVDSCommand<P extends ChangeDiskVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {
    private String isoLocation = "";

    public ChangeFloppyVDSCommand(P parameters) {
        super(parameters);
        isoLocation = parameters.getDiskPath();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmReturn = getBroker().changeFloppy(vmId.toString(), isoLocation);
        proceedProxyReturnValue();
        setReturnValue(VdsBrokerObjectsBuilder.buildVMDynamicData(vmReturn.vm, getVds()).getStatus());
    }
}
