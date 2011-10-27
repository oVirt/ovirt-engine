package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public final class IsoListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String ISO_LIST = "isolist";
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("isolist")]
    public String[] mVMList;

    public IsoListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] tempObj = (Object[]) innerMap.get(ISO_LIST);
        if (tempObj != null) {
            mVMList = new String[tempObj.length];
            for (int i = 0; i < tempObj.length; i++) {
                mVMList[i] = (String) tempObj[i];
            }
        }
    }
}
