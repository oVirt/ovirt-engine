package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class StatusReturnForXmlRpc {
    private static final String STATUS = "status";

    private StatusForXmlRpc mStatus;

    @SuppressWarnings("unchecked")
    public StatusReturnForXmlRpc(Map<String, Object> innerMap) {
        Map<String, Object> statusMap = (Map<String, Object>) innerMap.get(STATUS);
        mStatus = new StatusForXmlRpc(statusMap);
    }

    public StatusForXmlRpc getXmlRpcStatus() {
        return mStatus;
    }

    public void setXmlRpcStatus(StatusForXmlRpc status) {
        mStatus = status;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [mStatus=" + mStatus + "]";
    }
}
