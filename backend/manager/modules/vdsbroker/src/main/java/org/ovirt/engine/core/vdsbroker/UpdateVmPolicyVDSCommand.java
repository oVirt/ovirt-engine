package org.ovirt.engine.core.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.UpdateVmPolicyVDSParams;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;

public class UpdateVmPolicyVDSCommand<P extends UpdateVmPolicyVDSParams> extends VdsBrokerCommand<P> {

    public UpdateVmPolicyVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        getBroker().updateVmPolicy(build());
        proceedProxyReturnValue();
    }

    protected Map<String, Object> build() {
        Map<String, Object> struct = new HashMap<>();
        struct.put("vmId", getParameters().getVmId().toString());
        struct.put("vcpuLimit", String.valueOf(getParameters().getCpuLimit()));
        return struct;
    }

}
