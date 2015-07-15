package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public class UUIDListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String UUID_LIST = "uuidlist";
    private String[] uuidList;

    public UUIDListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] tempObj = (Object[]) innerMap.get(UUID_LIST);
        if (tempObj != null) {
            uuidList = new String[tempObj.length];
            for (int i = 0; i < tempObj.length; i++) {
                uuidList[i] = (String) tempObj[i];
            }
        }
    }

    public String[] getUUIDList() {
        return uuidList;
    }
}
