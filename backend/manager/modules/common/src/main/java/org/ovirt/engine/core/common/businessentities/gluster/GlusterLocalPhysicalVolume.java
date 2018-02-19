package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

public class GlusterLocalPhysicalVolume implements Serializable {
    private static final long serialVersionUID = -1134758927239004412L;

    private String physicalVolumeName;
    private String volumeGroupName;

    public String getPhysicalVolumeName() {
        return physicalVolumeName;
    }

    public void setPhysicalVolumeName(String physicalVolumeName) {
        this.physicalVolumeName = physicalVolumeName;
    }

    public String getVolumeGroupName() {
        return volumeGroupName;
    }

    public void setVolumeGroupName(String volumeGroupName) {
        this.volumeGroupName = volumeGroupName;
    }
}
