package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmCheckpointsVDSParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmCheckpointInfo;

public class DeleteVmCheckpointsVDSCommand<P extends VmCheckpointsVDSParameters> extends VdsBrokerCommand<P> {

    public DeleteVmCheckpointsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {

        VmCheckpointInfo vmCheckpointInfo = getBroker().deleteVmCheckpoints(
                getParameters().getVmId().toString(),
                getParameters().getCheckpointsIds().toArray(new String[0]));
        proceedProxyReturnValue();

        setReturnValue(vmCheckpointInfo);
    }
}
