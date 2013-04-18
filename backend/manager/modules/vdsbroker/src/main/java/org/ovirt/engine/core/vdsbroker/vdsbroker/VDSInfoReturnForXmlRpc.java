package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

//-----------------------------------------------------
//
//-----------------------------------------------------
@SuppressWarnings("unchecked")
public final class VDSInfoReturnForXmlRpc {
    private static final String STATUS = "status";
    private static final String INFO = "info";

    public StatusForXmlRpc mStatus;
    public Map<String, Object> mInfo;

    public VDSInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        mStatus = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));
        mInfo = (Map<String, Object>) innerMap.get(INFO);
    }
}
