package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

//-----------------------------------------------------
//
//-----------------------------------------------------

public final class ServerConnectionListReturn extends StatusReturn {
    private static final String STATUS_LIST = "serverList";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [MissingMapping(MappingAction.Ignore), Member("serverList")]
    public Map<String, Object>[] connectionList;

    @SuppressWarnings("unchecked")
    public ServerConnectionListReturn(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] temp = (Object[]) innerMap.get(STATUS_LIST);
        if (temp != null) {
            connectionList = new Map[temp.length];
            for (int i = 0; i < temp.length; i++) {
                connectionList[i] = (Map<String, Object>) temp[i];
            }
        }
    }

}
