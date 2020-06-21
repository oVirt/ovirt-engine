package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VmCheckpointVDSParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmCheckpointInfo;

public class GetVmCheckpointXMLVDSCommand<P extends VmCheckpointVDSParameters> extends VdsBrokerCommand<P> {
    private VmCheckpointInfo checkpointInfo;

    public GetVmCheckpointXMLVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        checkpointInfo = getBroker().getVmCheckpointsXML(getParameters().getVmId().toString(),
                getParameters().getCheckpoint().getId().toString());
        proceedProxyReturnValue();

        setReturnValue(checkpointInfo);
    }

    @Override
    protected Status getReturnStatus() {
        return checkpointInfo.getStatus();
    }
}
