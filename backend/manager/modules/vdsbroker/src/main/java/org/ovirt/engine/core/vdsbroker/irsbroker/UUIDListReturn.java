package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class UUIDListReturn extends StatusReturn {
    private static final String STATUS = "status";
    private static final String UUID_LIST = "uuidlist";

    private String[] uuidList;
    private Status status;

    public UUIDListReturn(Map<String, Object> innerMap) {
        super(innerMap);
        status = new Status((Map<String, Object>) innerMap.get(STATUS));

        Object[] tempObj = (Object[]) innerMap.get(UUID_LIST);
        if (tempObj != null) {
            uuidList = new String[tempObj.length];
            for (int i = 0; i < tempObj.length; i++) {
                uuidList[i] = (String) tempObj[i];
            }
        }
    }

    public Status getStatus() {
        return status;
    }

    public String[] getUUIDList() {
        return uuidList;
    }
}
