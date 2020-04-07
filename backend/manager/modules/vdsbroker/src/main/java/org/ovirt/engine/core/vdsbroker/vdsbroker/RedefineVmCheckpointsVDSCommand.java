package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.VmCheckpointsVDSParameters;
import org.ovirt.engine.core.vdsbroker.irsbroker.VmCheckpointInfo;

public class RedefineVmCheckpointsVDSCommand<P extends VmCheckpointsVDSParameters> extends VdsBrokerCommand<P> {
    VmCheckpointInfo vmCheckpointInfo;

    public RedefineVmCheckpointsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        vmCheckpointInfo = getBroker().redefineVmCheckpoints(
                getParameters().getVmId().toString(), createCheckpointsMap());
        proceedProxyReturnValue();

        setReturnValue(vmCheckpointInfo);
    }

    private HashMap[] createCheckpointsMap() {
        return getParameters().getCheckpoints().stream().map(checkpoint -> {
            Map<String, Object> params = new HashMap<>();
            params.put("id", checkpoint.getId().toString());
            params.put("xml", checkpoint.getCheckpointXml());
            return params;
        }).toArray(HashMap[]::new);
    }

    @Override
    protected Status getReturnStatus() {
        return vmCheckpointInfo.getStatus();
    }
}
