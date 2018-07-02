package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVDOVolume;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class GlusterVDOVolumeListReturn extends StatusReturn {
    private static final String VDO_VOLUME_LIST = "vdoVolumeList";
    private static final String VDO_VOLUME_NAME = "name";
    private static final String VDO_VOLUME_DEVICE = "device";
    private static final String VDO_VOLUME_SIZE = "size";
    private static final String VDO_VOLUME_FREE = "free";
    private static final String VDO_VOLUME_LOGICAL_BLOCKS = "logicalBytesUsed";
    private static final String VDO_VOLUME_PHYSICAL_BLOCKS = "physicalBytesUsed";

    private final List<GlusterVDOVolume> vdoVolumes = new ArrayList<>();

    public GlusterVDOVolumeListReturn(Map<String, Object> innerMap) {
        super(innerMap);

        Object[] volumeInfo = (Object[]) innerMap.get(VDO_VOLUME_LIST);
        for(Object volumeObject : volumeInfo) {
            Map<String, Object> volumeData = (Map<String, Object>) volumeObject;

            GlusterVDOVolume volume = new GlusterVDOVolume();
            volume.setName((String)volumeData.get(VDO_VOLUME_NAME));
            volume.setDevice((String)volumeData.get(VDO_VOLUME_DEVICE));
            volume.setSize((Number)volumeData.get(VDO_VOLUME_SIZE));
            volume.setFree((Number)volumeData.get(VDO_VOLUME_FREE));

            //This data may be missing, so some safe default values are needed.
            volume.setLogicalBlocks((Number)volumeData.getOrDefault(VDO_VOLUME_LOGICAL_BLOCKS, 0));
            volume.setPhysicalBlocks((Number)volumeData.getOrDefault(VDO_VOLUME_PHYSICAL_BLOCKS, 0));

            vdoVolumes.add(volume);
        }
    }

    public List<GlusterVDOVolume> getVdoVolumes() {
        return vdoVolumes;
    }
}
