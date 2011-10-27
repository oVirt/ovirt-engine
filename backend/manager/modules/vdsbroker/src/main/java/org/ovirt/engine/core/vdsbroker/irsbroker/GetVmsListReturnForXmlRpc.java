package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public final class GetVmsListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String VM_LIST = "vmlist";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("vmlist")]
    public String[] vmlist;

    @SuppressWarnings("unchecked")
    public GetVmsListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] tempObj = (Object[]) innerMap.get(VM_LIST);
        if (tempObj != null) {
            vmlist = new String[tempObj.length];
            for (int i = 0; i < tempObj.length; i++) {
                vmlist[i] = (String) tempObj[i];
            }
        }

    }
}
