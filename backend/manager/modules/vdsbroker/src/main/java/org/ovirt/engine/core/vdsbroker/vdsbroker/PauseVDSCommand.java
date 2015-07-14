package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.PauseVDSCommandParameters;

public class PauseVDSCommand<P extends PauseVDSCommandParameters> extends VmReturnVdsBrokerCommand<P> {
    public PauseVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmReturn = getBroker().pause(vmId.toString());
        proceedProxyReturnValue();
        setReturnValue(VdsBrokerObjectsBuilder.buildVMDynamicData(vmReturn.vm, getVds()).getStatus());
    }
}
