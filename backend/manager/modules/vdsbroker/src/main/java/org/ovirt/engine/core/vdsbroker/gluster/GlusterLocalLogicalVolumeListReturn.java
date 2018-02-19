package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterLocalLogicalVolume;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class GlusterLocalLogicalVolumeListReturn extends StatusReturn {
    private static final String LOGICAL_VOLUME_LIST = "logicalVolumeList";
    private static final String LOGICAL_VOLUME_NAME = "lv_name";
    private static final String VOLUME_GROUP_NAME = "vg_name";
    private static final String VOLUME_POOL_NAME = "pool_lv";
    private static final String VOLUME_SIZE = "lv_size";
    private static final String VOLUME_FREE = "lv_free";

    private final List<GlusterLocalLogicalVolume> logicalVolumes = new ArrayList<>();

    public GlusterLocalLogicalVolumeListReturn(Map<String, Object> innerMap) {
        super(innerMap);

        Object[] volumeInfo = (Object[]) innerMap.get(LOGICAL_VOLUME_LIST);
        for(Object volumeObject : volumeInfo) {
            Map<String, Object> volumeData = (Map<String, Object>) volumeObject;

            GlusterLocalLogicalVolume volume = new GlusterLocalLogicalVolume();
            volume.setLogicalVolumeName((String) volumeData.get(LOGICAL_VOLUME_NAME));
            volume.setVolumeGroupName((String) volumeData.get(VOLUME_GROUP_NAME));
            volume.setPoolName((String) volumeData.get(VOLUME_POOL_NAME));
            volume.setSize((Number) volumeData.get(VOLUME_SIZE));
            volume.setFree((Number) volumeData.get(VOLUME_FREE));

            logicalVolumes.add(volume);
        }
    }

    public List<GlusterLocalLogicalVolume> getLogicalVolumes() {
        return logicalVolumes;
    }
}
