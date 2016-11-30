package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class ResizeStorageDomainPVMapReturn extends StatusReturn {
    private static final String SIZE = "size";

    private Long deviceSize;

    public ResizeStorageDomainPVMapReturn(Map<String, Object> innerMap) {
        super(innerMap);
        deviceSize = (innerMap.get(SIZE) != null) ?
                Long.valueOf((String) innerMap.get(SIZE)) : null;
    }

    public Long getDeviceSize() {
        return deviceSize;
    }
}
