package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

//-----------------------------------------------------
//
//-----------------------------------------------------
@SuppressWarnings("unchecked")
public final class VDSInfoReturn {
    private static final String STATUS = "status";
    private static final String INFO = "info";

    public Status status;
    public Map<String, Object> info;

    public VDSInfoReturn() {
    }

    public VDSInfoReturn(Map<String, Object> innerMap) {
        status = new Status((Map<String, Object>) innerMap.get(STATUS));
        info = (Map<String, Object>) innerMap.get(INFO);
    }

    public VDSInfoReturn withStatus(Map<String, Object> statusMap) {
        status = new Status(statusMap);
        return this;
    }
}
