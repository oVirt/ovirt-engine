package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

public class GlusterLocalLogicalVolume implements Serializable {
    private static final long serialVersionUID = -1134758927239004412L;

    private String logicalVolumeName;
    private String volumeGroupName;
    private String poolName;
    private long size;
    private long free;

    public String getLogicalVolumeName() {
        return logicalVolumeName;
    }

    public void setLogicalVolumeName(String logicalVolumeName) {
        this.logicalVolumeName = logicalVolumeName;
    }

    public String getVolumeGroupName() {
        return volumeGroupName;
    }

    public void setVolumeGroupName(String volumeGroupName) {
        this.volumeGroupName = volumeGroupName;
    }

    public String getPoolName() {
        return poolName;
    }

    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(Number size) {
        this.size = size.longValue();
    }

    public long getFree() {
        return free;
    }

    public void setFree(Number free) {
        this.free = free.longValue();
    }
}
