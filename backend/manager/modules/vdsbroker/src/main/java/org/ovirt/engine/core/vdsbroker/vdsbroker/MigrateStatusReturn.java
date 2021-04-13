package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

public class MigrateStatusReturn {

    private static final String STATUS = "status";
    private static final String RESPONSE = "response";
    private static final String DOWNTIME = "downtime";
    private static final String MESSAGE = "message";

    private Status status;
    public Integer downtime;
    private String message;

    @SuppressWarnings("unchecked")
    public MigrateStatusReturn(Map<String, Object> innerMap) {
        status = new Status((Map<String, Object>) innerMap.get(STATUS));
        if (innerMap.containsKey(RESPONSE)) {
            Map<String, Object> response = (Map<String, Object>) innerMap.get(RESPONSE);
            if (response.containsKey(STATUS)) {
                Map<String, Object> status = (Map<String, Object>) response.get(STATUS);
                if (status.containsKey(MESSAGE)) {
                    message = (String) status.get(MESSAGE);
                }
            }
            if (response.containsKey(DOWNTIME)) {
                downtime = (Integer) response.get(DOWNTIME);
            }
        }
    }

    public Status getStatus() {
        return status;
    }

    public Integer getDowntime() {
        return downtime;
    }

    public String getMessage() {
        return message;
    }

}
