package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.vdsbroker.*;

public class StatusReturnForXmlRpc {
    private static final String STATUS = "status";

    public StatusForXmlRpc mStatus;

    public StatusReturnForXmlRpc(Map<String, Object> innerMap) {
        Map<String, Object> statusMap = (Map<String, Object>) innerMap.get(STATUS);
        mStatus = new StatusForXmlRpc(statusMap);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [mStatus=" + mStatus + "]";
    }

}
