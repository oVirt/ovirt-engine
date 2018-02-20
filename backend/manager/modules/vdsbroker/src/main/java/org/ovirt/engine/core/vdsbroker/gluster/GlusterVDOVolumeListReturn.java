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

            vdoVolumes.add(volume);
        }
    }

    public List<GlusterVDOVolume> getVdoVolumes() {
        return vdoVolumes;
    }
}
