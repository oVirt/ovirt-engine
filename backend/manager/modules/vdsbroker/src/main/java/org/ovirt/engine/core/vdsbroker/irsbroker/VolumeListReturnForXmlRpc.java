package org.ovirt.engine.core.vdsbroker.irsbroker;

import java.util.Map;

public final class VolumeListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String VOLUMES = "volumes";
    public String[] volumeList;

    @SuppressWarnings("unchecked")
    public VolumeListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object[] tempObj = (Object[]) innerMap.get(VOLUMES);
        if (tempObj != null) {
            volumeList = new String[tempObj.length];
            for (int i = 0; i < tempObj.length; i++) {
                volumeList[i] = (String) tempObj[i];
            }
        }
    }

    public String[] getVolumeList() {
        return volumeList;
    }
}
