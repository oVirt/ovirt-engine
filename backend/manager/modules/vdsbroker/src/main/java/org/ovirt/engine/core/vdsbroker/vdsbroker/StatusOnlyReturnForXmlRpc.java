package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

public final class StatusOnlyReturnForXmlRpc {

    private static final String STATUS = "status";

    public StatusForXmlRpc mStatus;

    public StatusOnlyReturnForXmlRpc(Map<String, Object> innerMap) {
        Map<String, Object> statusMap = (Map<String, Object>) innerMap.get(STATUS);
        mStatus = new StatusForXmlRpc(statusMap);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [mStatus=" + mStatus + "]";
    }
}
