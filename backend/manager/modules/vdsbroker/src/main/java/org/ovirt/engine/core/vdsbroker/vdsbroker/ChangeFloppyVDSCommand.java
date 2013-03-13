package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class ChangeFloppyVDSCommand<P extends ChangeDiskVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {
    private String mIsoLocation = "";

    public ChangeFloppyVDSCommand(P parameters) {
        super(parameters);
        mIsoLocation = parameters.getDiskPath();
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        mVmReturn = getBroker().changeFloppy(mVmId.toString(), mIsoLocation);
        ProceedProxyReturnValue();
        setReturnValue(VdsBrokerObjectsBuilder.buildVMDynamicData(mVmReturn.mVm).getStatus());
    }
}
