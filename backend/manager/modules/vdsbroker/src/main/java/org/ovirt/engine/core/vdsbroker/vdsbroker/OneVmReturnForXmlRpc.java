package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

@SuppressWarnings("unchecked")
public final class OneVmReturnForXmlRpc {

    private static final String STATUS = "status";
    private static final String VM_LIST = "vmList";

    public StatusForXmlRpc status;
    public Map<String, Object> vm;

    public OneVmReturnForXmlRpc(Map<String, Object> innerMap) {
        status = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));
        vm = (Map<String, Object>) innerMap.get(VM_LIST);
    }
}
