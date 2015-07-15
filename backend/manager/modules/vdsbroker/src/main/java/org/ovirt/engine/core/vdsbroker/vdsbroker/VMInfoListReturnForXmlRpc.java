package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class VMInfoListReturnForXmlRpc {
    private static final String STATUS = "status";
    private static final String STATS_LIST = "statsList";

    public StatusForXmlRpc status;
    public Map<String, Object>[] infoList;

    public VMInfoListReturnForXmlRpc(Map<String, Object> innerMap) {
        status = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));
        Object[] temp = (Object[]) innerMap.get(STATS_LIST);
        if (temp != null) {
            infoList = new HashMap[temp.length];
            for (int i = 0; i < temp.length; i++) {
                infoList[i] = (Map<String, Object>) temp[i];
            }
        }
    }

}
