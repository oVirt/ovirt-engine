package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterLocalPhysicalVolume;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class GlusterLocalPhysicalVolumeListReturn extends StatusReturn {
    private static final String PHYSICAL_VOLUME_LIST = "physicalVolumeList";
    private static final String PHYSICAL_VOLUME_NAME = "pv_name";
    private static final String VOLUME_GROUP_NAME = "vg_name";

    private final List<GlusterLocalPhysicalVolume> physicalVolumes = new ArrayList<>();

    public GlusterLocalPhysicalVolumeListReturn(Map<String, Object> innerMap) {
        super(innerMap);

        Object[] volumeInfo = (Object[]) innerMap.get(PHYSICAL_VOLUME_LIST);
        for(Object volumeObject : volumeInfo) {
            Map<String, Object> volumeData = (Map<String, Object>) volumeObject;

            GlusterLocalPhysicalVolume volume = new GlusterLocalPhysicalVolume();
            volume.setPhysicalVolumeName((String) volumeData.get(PHYSICAL_VOLUME_NAME));
            volume.setVolumeGroupName((String) volumeData.get(VOLUME_GROUP_NAME));

            physicalVolumes.add(volume);
        }
    }

    public List<GlusterLocalPhysicalVolume> getPhysicalVolumes() {
        return physicalVolumes;
    }

}
