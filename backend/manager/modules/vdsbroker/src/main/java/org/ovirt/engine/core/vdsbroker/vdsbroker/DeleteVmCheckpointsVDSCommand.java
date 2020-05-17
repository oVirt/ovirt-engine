package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmCheckpointsVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmCheckpointInfo;

public class DeleteVmCheckpointsVDSCommand<P extends VmCheckpointsVDSParameters> extends VdsBrokerCommand<P> {
    VmCheckpointInfo vmCheckpointInfo;

    public DeleteVmCheckpointsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmCheckpointInfo = getBroker().deleteVmCheckpoints(
                getParameters().getVmId().toString(),
                getParameters().getCheckpointsIds().stream()
                        .map(Guid::toString).toArray(String[]::new));
        proceedProxyReturnValue();

        setReturnValue(vmCheckpointInfo);
    }

    @Override
    protected Status getReturnStatus() {
        return vmCheckpointInfo.getStatus();
    }
}
