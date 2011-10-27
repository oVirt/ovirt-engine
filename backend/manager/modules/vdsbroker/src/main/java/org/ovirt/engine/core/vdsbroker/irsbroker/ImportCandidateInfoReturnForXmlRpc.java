package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class ImportCandidateInfoReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String VM_INFO = "vminfo";
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("vminfo")]
    public XmlRpcStruct mInfoList;

    @SuppressWarnings("unchecked")
    public ImportCandidateInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(VM_INFO);
        if (temp != null) {
            mInfoList = new XmlRpcStruct((Map<String, Object>) temp);
        }
    }
}
