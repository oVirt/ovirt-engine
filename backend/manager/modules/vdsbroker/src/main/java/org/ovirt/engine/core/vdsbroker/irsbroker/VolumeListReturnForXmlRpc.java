package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public final class VolumeListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String VOLUMES = "volumes";
    public String[] mVolumeList;

    @SuppressWarnings("unchecked")
    public VolumeListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] tempObj = (Object[]) innerMap.get(VOLUMES);
        if (tempObj != null) {
            mVolumeList = new String[tempObj.length];
            for (int i = 0; i < tempObj.length; i++) {
                mVolumeList[i] = (String) tempObj[i];
            }
        }
    }

    public String[] getVolumeList() {
        return mVolumeList;
    }
}
