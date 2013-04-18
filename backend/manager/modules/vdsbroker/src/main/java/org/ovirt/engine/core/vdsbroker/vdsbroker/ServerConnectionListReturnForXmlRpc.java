package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;

//-----------------------------------------------------
//
//-----------------------------------------------------

public final class ServerConnectionListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String STATUS_LIST = "serverList";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("serverList")]
    public Map<String, Object>[] mConnectionList;

    @SuppressWarnings("unchecked")
    public ServerConnectionListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] temp = (Object[]) innerMap.get(STATUS_LIST);
        if (temp != null) {
            mConnectionList = new Map[temp.length];
            for (int i = 0; i < temp.length; i++) {
                mConnectionList[i] = (Map<String, Object>) temp[i];
            }
        }
    }

}
