package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class PauseVDSCommand<P extends PauseVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {
    public PauseVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        mVmReturn = getBroker().pause(mVmId.toString());
        ProceedProxyReturnValue();
        setReturnValue(VdsBrokerObjectsBuilder.buildVMDynamicData(mVmReturn.mVm).getStatus());
    }
}
