package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public final class IrsVMListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String VM_LIST = "vmlist";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("vmlist")]
    public String[] mVMList;

    @SuppressWarnings("unchecked")
    public IrsVMListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] tempObj = (Object[]) innerMap.get(VM_LIST);
        if (tempObj != null) {
            mVMList = new String[tempObj.length];
            for (int i = 0; i < tempObj.length; i++) {
                mVMList[i] = (String) tempObj[i];
            }
        }
    }
}
