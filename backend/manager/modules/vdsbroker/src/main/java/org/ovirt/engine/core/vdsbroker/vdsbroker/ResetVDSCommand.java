package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class ResetVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VmReturnVdsBrokerCommand<P> {
    public ResetVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        mVmReturn = getBroker().reset(mVmId.toString());
        ProceedProxyReturnValue();
        setReturnValue(VdsBrokerObjectsBuilder.buildVMDynamicData(mVmReturn.mVm));
    }
}
