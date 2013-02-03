package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public class UUIDListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String UUID_LIST = "uuidlist";
    private String[] mUUIDList;

    public UUIDListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] tempObj = (Object[]) innerMap.get(UUID_LIST);
        if (tempObj != null) {
            mUUIDList = new String[tempObj.length];
            for (int i = 0; i < tempObj.length; i++) {
                mUUIDList[i] = (String) tempObj[i];
            }
        }
    }

    public String[] getUUIDList() {
        return mUUIDList;
    }
}
