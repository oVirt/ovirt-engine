package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.vdscommands.MoveStorageDomainDeviceVDSCommandParameters;

public class MoveStorageDomainDeviceVDSCommand<P extends MoveStorageDomainDeviceVDSCommandParameters>
        extends VdsBrokerCommand<P> {
    public MoveStorageDomainDeviceVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().moveDomainDevice(getParameters().getJobId().toString(), buildMoveParams());

        proceedProxyReturnValue();
    }

    private Map<String, Object> buildMoveParams() {
        Map<String, Object> info = new HashMap<>();
        info.put("sd_id", getParameters().getStorageDomainId().toString());
        info.put("src_guid", getParameters().getSrcDeviceId().toString());
        if (getParameters().getDstDevicesIds() != null) {
            info.put("dst_guids", getParameters().getDstDevicesIds());
        }
        return info;
    }
}
