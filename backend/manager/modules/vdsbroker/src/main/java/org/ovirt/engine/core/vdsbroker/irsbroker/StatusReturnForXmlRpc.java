package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.vdsbroker.jsonrpc.FutureMap;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class StatusReturnForXmlRpc {
    private static final String STATUS = "status";

    private StatusForXmlRpc status;
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
        if (status == null) {
            Map<String, Object> statusMap = (Map<String, Object>) innerMap.get(STATUS);
            status = new StatusForXmlRpc(statusMap);
        }
        return status;
    }

    public void setXmlRpcStatus(StatusForXmlRpc status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("status", isRequestCompleted() ? getXmlRpcStatus() : "Pending Response")
                .build();
    }
}
