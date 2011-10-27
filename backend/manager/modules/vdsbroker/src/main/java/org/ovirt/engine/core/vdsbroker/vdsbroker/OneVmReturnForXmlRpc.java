package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class OneVmReturnForXmlRpc {

    private static final String STATUS = "status";
    private static final String VM_LIST = "vmList";

    // [XmlRpcMember("status")]
    public StatusForXmlRpc mStatus;

    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("vmList")]
    public XmlRpcStruct mVm;

    @SuppressWarnings("unchecked")
    public OneVmReturnForXmlRpc(Map<String, Object> innerMap) {
        mStatus = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));
        Object temp = innerMap.get(VM_LIST);
        if (temp == null) {
            mVm = null;
        } else {
            mVm = new XmlRpcStruct((Map) temp);
        }

    }

}
