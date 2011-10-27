package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.*;

public class PowerDownVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VmReturnVdsBrokerCommand<P> {
    public PowerDownVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        mVmReturn = getBroker().powerDown(mVmId.toString());
        ProceedProxyReturnValue();
        setReturnValue(VdsBrokerObjectsBuilder.buildVMDynamicData(mVmReturn.mVm));
    }
}
