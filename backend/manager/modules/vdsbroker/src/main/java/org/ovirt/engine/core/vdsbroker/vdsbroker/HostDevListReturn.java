package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

@SuppressWarnings("unchecked")
public class HostDevListReturn {

    public final Status status;
    public final Map<String, Map<String, Map<String, Object>>> devices;

    public HostDevListReturn(Map<String, Object> innerMap) {
        status = new Status((Map<String, Object>) innerMap.get(VdsProperties.status));
        devices = (Map<String, Map<String, Map<String, Object>>>) innerMap.get(VdsProperties.DEVICE_LIST);
    }
}
