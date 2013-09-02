package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.PauseVDSCommandParameters;

public class PauseVDSCommand<P extends PauseVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {
    public PauseVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        mVmReturn = getBroker().pause(mVmId.toString());
        proceedProxyReturnValue();
        setReturnValue(VdsBrokerObjectsBuilder.buildVMDynamicData(mVmReturn.mVm).getStatus());
    }
}
