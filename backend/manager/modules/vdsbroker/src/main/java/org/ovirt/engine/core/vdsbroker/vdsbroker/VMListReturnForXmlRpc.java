package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

//-----------------------------------------------------
//
//-----------------------------------------------------

//-----------------------------------------------------
//
//-----------------------------------------------------

public final class VMListReturnForXmlRpc {

    private static final String STATUS = "status";
    private static final String VM_LIST = "vmList";

    // [XmlRpcMember("status")]
    public StatusForXmlRpc mStatus;

    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("vmList")]
    public XmlRpcStruct[] mVmList;

    @SuppressWarnings("unchecked")
    public VMListReturnForXmlRpc(Map<String, Object> innerMap) {
        mStatus = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));
        Object[] temp = (Object[]) innerMap.get(VM_LIST);
        if (temp != null) {
            mVmList = new XmlRpcStruct[temp.length];
            for (int i = 0; i < temp.length; i++) {
                mVmList[i] = new XmlRpcStruct((Map<String, Object>) temp[i]);
            }
        }
    }

}
