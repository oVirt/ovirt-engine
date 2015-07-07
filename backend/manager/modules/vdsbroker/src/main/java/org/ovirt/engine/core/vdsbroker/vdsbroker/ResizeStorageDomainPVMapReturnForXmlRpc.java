package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public class ResizeStorageDomainPVMapReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String SIZE = "size";

    private Long deviceSize;

    public ResizeStorageDomainPVMapReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        deviceSize = (innerMap.get(SIZE) != null) ?
                Long.valueOf((String) innerMap.get(SIZE)) : null;
    }

    public Long getDeviceSize() {
        return deviceSize;
    }
}
