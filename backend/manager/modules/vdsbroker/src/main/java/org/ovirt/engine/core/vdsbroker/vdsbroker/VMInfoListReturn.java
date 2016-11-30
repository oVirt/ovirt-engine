package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class VMInfoListReturn {
    private static final String STATUS = "status";
    private static final String STATS_LIST = "statsList";

    public Status status;
    public Map<String, Object>[] infoList;

    public VMInfoListReturn(Map<String, Object> innerMap) {
        status = new Status((Map<String, Object>) innerMap.get(STATUS));
        Object[] temp = (Object[]) innerMap.get(STATS_LIST);
        if (temp != null) {
            infoList = new HashMap[temp.length];
            for (int i = 0; i < temp.length; i++) {
                infoList[i] = (Map<String, Object>) temp[i];
            }
        }
    }

}
