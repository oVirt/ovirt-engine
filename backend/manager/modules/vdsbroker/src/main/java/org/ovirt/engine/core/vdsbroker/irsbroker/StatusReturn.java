package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.vdsbroker.jsonrpc.FutureMap;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class StatusReturn {
    private static final String STATUS = "status";

    private Status status;
    protected Map<String, Object> innerMap;

    public StatusReturn(Map<String, Object> innerMap) {
        this.innerMap = innerMap;
    }

    public boolean isRequestCompleted() {
        if (innerMap instanceof FutureMap) {
            return ((FutureMap) innerMap).isRequestCompleted();
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public Status getStatus() {
        if (status == null) {
            Map<String, Object> statusMap = (Map<String, Object>) innerMap.get(STATUS);
            status = new Status(statusMap);
        }
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("status", isRequestCompleted() ? getStatus() : "Pending Response")
                .build();
    }
}
