package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

public class MigrateStatusReturn {

    private static final String STATUS = "status";
    private static final String RESPONSE = "response";
    private static final String DOWNTIME = "downtime";

    private Status status;
    public Integer downtime;

    @SuppressWarnings("unchecked")
    public MigrateStatusReturn(Map<String, Object> innerMap) {
        status = new Status((Map<String, Object>) innerMap.get(STATUS));
        if (innerMap.containsKey(RESPONSE)) {
            Map<String, Object> response = (Map<String, Object>)innerMap.get(RESPONSE);
            if (response.containsKey(DOWNTIME)) {
                downtime = (Integer)response.get(DOWNTIME);
            }
        }
    }

    public Status getStatus() {
        return status;
    }

    public Integer getDowntime() {
        return downtime;
    }
}
