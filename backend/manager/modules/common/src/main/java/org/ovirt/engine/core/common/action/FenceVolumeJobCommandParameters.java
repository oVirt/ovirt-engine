package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;

public class FenceVolumeJobCommandParameters extends StorageJobCommandParameters {
    private VdsmImageLocationInfo imageLocationInfo;

    public FenceVolumeJobCommandParameters() {
    }

    public FenceVolumeJobCommandParameters(VdsmImageLocationInfo imageLocationInfo) {
        this.imageLocationInfo = imageLocationInfo;
    }

    public VdsmImageLocationInfo getImageLocationInfo() {
        return imageLocationInfo;
    }

    public void setImageLocationInfo(VdsmImageLocationInfo imageLocationInfo) {
        this.imageLocationInfo = imageLocationInfo;
    }
}
