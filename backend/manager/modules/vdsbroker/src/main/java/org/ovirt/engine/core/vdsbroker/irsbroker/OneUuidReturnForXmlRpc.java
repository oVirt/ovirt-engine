package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

//-----------------------------------------------------
//
//-----------------------------------------------------

public final class OneUuidReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String UUID = "uuid";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("uuid")]
    public String uuid;

    public OneUuidReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        uuid = (String) innerMap.get(UUID);
    }

}
