package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

//-----------------------------------------------------
//
//-----------------------------------------------------

//-----------------------------------------------------
//
//-----------------------------------------------------
@SuppressWarnings("unchecked")
public final class VMListReturnForXmlRpc {

    private static final String STATUS = "status";
    private static final String VM_LIST = "vmList";
    private static final Map<String, Object>[] EMPTY_ARRAY_OF_MAPS = new HashMap[0];

    public StatusForXmlRpc mStatus;
    public Map<String, Object>[] mVmList = EMPTY_ARRAY_OF_MAPS;

    public VMListReturnForXmlRpc(Map<String, Object> innerMap) {
        mStatus = new StatusForXmlRpc((Map<String, Object>) innerMap.get(STATUS));
        Object[] temp = (Object[]) innerMap.get(VM_LIST);
        if (temp != null) {
            mVmList = new HashMap[temp.length];
            for (int i = 0; i < temp.length; i++) {
                mVmList[i] = (Map<String, Object>) temp[i];
            }
        }
    }

}
