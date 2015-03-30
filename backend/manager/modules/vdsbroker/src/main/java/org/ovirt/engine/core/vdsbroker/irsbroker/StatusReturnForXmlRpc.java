package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.vdsbroker.jsonrpc.FutureMap;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class StatusReturnForXmlRpc {
    private static final String STATUS = "status";

    private StatusForXmlRpc mStatus;
    protected Map<String, Object> innerMap;

    public StatusReturnForXmlRpc(Map<String, Object> innerMap) {
        this.innerMap = innerMap;
    }

    public boolean isRequestCompleted() {
        if (innerMap instanceof FutureMap) {
            return ((FutureMap) innerMap).isRequestCompleted();
        }
        return true;
    }

    public StatusForXmlRpc getXmlRpcStatus() {
        if (mStatus == null) {
            Map<String, Object> statusMap = (Map<String, Object>) innerMap.get(STATUS);
            mStatus = new StatusForXmlRpc(statusMap);
        }
        return mStatus;
    }

    public void setXmlRpcStatus(StatusForXmlRpc status) {
        mStatus = status;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("mStatus", (isRequestCompleted() ? getXmlRpcStatus() : "Pending Response"))
                .build();
    }
}
