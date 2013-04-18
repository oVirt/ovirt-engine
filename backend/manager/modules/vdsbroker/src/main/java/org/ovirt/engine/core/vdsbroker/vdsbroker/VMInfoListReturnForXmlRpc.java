package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class VMInfoListReturnForXmlRpc {
    private static final String STATUS = "status";
    private static final String STATS_LIST = "statsList";

    public StatusForXmlRpc mStatus;
    public Map<String, Object>[] mInfoList;

    public VMInfoListReturnForXmlRpc(Map<String, Object> innerMap) {
        mStatus = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));
        Object[] temp = (Object[]) innerMap.get(STATS_LIST);
        if (temp != null) {
            mInfoList = new HashMap[temp.length];
            for (int i = 0; i < temp.length; i++) {
                mInfoList[i] = (Map<String, Object>) temp[i];
            }
        }
    }

}
