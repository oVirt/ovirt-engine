package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmCheckpointsVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmCheckpointIds;

public class DeleteVmCheckpointsVDSCommand<P extends VmCheckpointsVDSParameters> extends VdsBrokerCommand<P> {
    VmCheckpointIds vmCheckpointIds;

    public DeleteVmCheckpointsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmCheckpointIds = getBroker().deleteVmCheckpoints(
                getParameters().getVmId().toString(),
                getParameters().getCheckpointsIds().stream()
                        .map(Guid::toString).toArray(String[]::new));
        proceedProxyReturnValue();

        setReturnValue(vmCheckpointIds);
    }

    @Override
    protected Status getReturnStatus() {
        return vmCheckpointIds.getStatus();
    }
}
