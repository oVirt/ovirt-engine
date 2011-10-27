package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;
import org.ovirt.engine.core.utils.ObjectDescriptor;

public final class StatusOnlyReturnForXmlRpc {

    private static final String STATUS = "status";

    // [XmlRpcMember("status")]
    public StatusForXmlRpc mStatus;

    @Override
    public String toString() {
        return ObjectDescriptor.toString(this);
    }

    public StatusOnlyReturnForXmlRpc(Map<String, Object> innerMap) {
        Map<String, Object> statusMap = (Map<String, Object>) innerMap.get(STATUS);
        mStatus = new StatusForXmlRpc(statusMap);
    }

}
