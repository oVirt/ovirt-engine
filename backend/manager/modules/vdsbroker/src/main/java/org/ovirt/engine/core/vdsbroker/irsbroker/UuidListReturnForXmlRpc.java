package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public final class UuidListReturnForXmlRpc extends StatusReturnForXmlRpc {

    private static final String UUID_LIST = "uuidlist";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("uuidlist")]
    public String[] mUuidList;

    public UuidListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] tempObj = (Object[]) innerMap.get(UUID_LIST);
        if (tempObj != null) {
            mUuidList = new String[tempObj.length];
            for (int i = 0; i < tempObj.length; i++) {
                mUuidList[i] = (String) tempObj[i];
            }
        }

    }

}
