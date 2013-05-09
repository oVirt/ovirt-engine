package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;

public class ResumeBrokerVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VmReturnVdsBrokerCommand<P> {
    public ResumeBrokerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        mVmReturn = getBroker().resume(mVmId.toString());
        ProceedProxyReturnValue();
    }
}
