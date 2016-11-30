package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class VMListReturn {

    private static final String STATUS = "status";
    private static final String VM_LIST = "vmList";
    private static final Map<String, Object>[] EMPTY_ARRAY_OF_MAPS = new HashMap[0];

    public Status status;
    public Map<String, Object>[] vmList = EMPTY_ARRAY_OF_MAPS;

    public VMListReturn(Map<String, Object> innerMap) {
        status = new Status((Map<String, Object>) innerMap.get(STATUS));
        Object[] temp = (Object[]) innerMap.get(VM_LIST);
        if (temp != null) {
            vmList = new HashMap[temp.length];
            for (int i = 0; i < temp.length; i++) {
                // 1196327: we need to process both types of list message
                // when temp[i] is a String we process array of vmids
                // when temp[i] is a Map we process a map with status to vmid mapping
                if (String.class.isInstance(temp[i])) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("vmId", temp[i]);
                    vmList[i] = map;
                } else {
                    vmList[i] = (Map<String, Object>) temp[i];
                }
            }
        }
    }

}
