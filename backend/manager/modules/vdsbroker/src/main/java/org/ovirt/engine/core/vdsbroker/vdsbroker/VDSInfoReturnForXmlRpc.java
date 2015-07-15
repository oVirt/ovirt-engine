package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

//-----------------------------------------------------
//
//-----------------------------------------------------
@SuppressWarnings("unchecked")
public final class VDSInfoReturnForXmlRpc {
    private static final String STATUS = "status";
    private static final String INFO = "info";

    public StatusForXmlRpc status;
    public Map<String, Object> info;

    public VDSInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        status = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));
        info = (Map<String, Object>) innerMap.get(INFO);
    }
}
