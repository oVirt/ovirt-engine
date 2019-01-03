package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmCheckpointsVDSParameters;

public class DeleteVmCheckpointsVDSCommand<P extends VmCheckpointsVDSParameters> extends VdsBrokerCommand<P> {

    public DeleteVmCheckpointsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {

        status = getBroker().deleteVmCheckpoints(
                getParameters().getVmId().toString(),
                getParameters().getCheckpointsIds().toArray(new String[0]));
        proceedProxyReturnValue();
    }
}
