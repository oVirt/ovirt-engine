package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

public final class StatusOnlyReturn {

    private static final String STATUS = "status";

    public Status status;

    @SuppressWarnings("unchecked")
    public StatusOnlyReturn(Map<String, Object> innerMap) {
        Map<String, Object> statusMap = (Map<String, Object>) innerMap.get(STATUS);
        status = new Status(statusMap);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [status=" + status + "]";
    }
}
