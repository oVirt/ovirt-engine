package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class ImportCandidatesInfoReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String VM_LIST = "vmlist";
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("vmlist")]
    public XmlRpcStruct mInfoList;

    @SuppressWarnings("unchecked")
    public ImportCandidatesInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(VM_LIST);
        if (temp != null) {
            mInfoList = new XmlRpcStruct((Map<String, Object>) temp);
        }
    }
}
