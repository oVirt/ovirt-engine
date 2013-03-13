package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class ChangeDiskVDSCommand<P extends ChangeDiskVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {
    private String mIsoLocation = "";

    public ChangeDiskVDSCommand(P parameters) {
        super(parameters);
        mIsoLocation = parameters.getDiskPath();
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        mVmReturn = getBroker().changeDisk(mVmId.toString(), mIsoLocation);
        ProceedProxyReturnValue();
        setReturnValue(VdsBrokerObjectsBuilder.buildVMDynamicData(mVmReturn.mVm).getStatus());
    }
}
