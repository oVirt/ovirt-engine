package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.ChangeDiskVDSCommandParameters;

public class ChangeDiskVDSCommand<P extends ChangeDiskVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {
    private String isoLocation = "";

    public ChangeDiskVDSCommand(P parameters) {
        super(parameters);
        isoLocation = parameters.getDiskPath();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmReturn = getBroker().changeDisk(vmId.toString(), isoLocation);
        proceedProxyReturnValue();
        setReturnValue(VdsBrokerObjectsBuilder.buildVMDynamicData(vmReturn.vm, getVds()).getStatus());
    }
}
