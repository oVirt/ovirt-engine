package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

@SuppressWarnings("unchecked")
public final class OneVmReturn {

    private static final String STATUS = "status";
    private static final String VM_LIST = "vmList";

    public Status status;
    public Map<String, Object> vm;

    public OneVmReturn(Map<String, Object> innerMap) {
        status = new Status((Map<String, Object>) innerMap.get(STATUS));
        vm = (Map<String, Object>) innerMap.get(VM_LIST);
    }
}
