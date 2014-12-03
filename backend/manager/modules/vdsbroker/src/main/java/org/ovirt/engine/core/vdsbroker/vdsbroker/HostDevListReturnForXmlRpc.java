package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

@SuppressWarnings("unchecked")
public class HostDevListReturnForXmlRpc {

    public final StatusForXmlRpc status;
    public final Map<String, Map<String, Map<String, Object>>> devices;

    public HostDevListReturnForXmlRpc(Map<String, Object> innerMap) {
        status = new StatusForXmlRpc((Map<String, Object>) innerMap.get(VdsProperties.status));
        devices = (Map<String, Map<String, Map<String, Object>>>) innerMap.get(VdsProperties.DEVICE_LIST);
    }
}
