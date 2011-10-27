package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

//-----------------------------------------------------
//
//-----------------------------------------------------

public final class VDSInfoReturnForXmlRpc {
    private static final String STATUS = "status";
    private static final String INFO = "info";

    // [XmlRpcMember("status")]
    public StatusForXmlRpc mStatus;
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("info")]
    public XmlRpcStruct mInfo;

    @SuppressWarnings("unchecked")
    public VDSInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        mStatus = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));
        Object temp = innerMap.get(INFO);
        if (temp != null) {
            mInfo = new XmlRpcStruct((Map<String, Object>) temp);
        }
    }

}
