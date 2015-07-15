package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

public final class StatusOnlyReturnForXmlRpc {

    private static final String STATUS = "status";

    public StatusForXmlRpc status;

    public StatusOnlyReturnForXmlRpc(Map<String, Object> innerMap) {
        Map<String, Object> statusMap = (Map<String, Object>) innerMap.get(STATUS);
        status = new StatusForXmlRpc(statusMap);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [status=" + status + "]";
    }
}
