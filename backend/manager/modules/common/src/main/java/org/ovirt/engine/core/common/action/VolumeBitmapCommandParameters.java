package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VdsmImageLocationInfo;
import org.ovirt.engine.core.compat.Guid;

public class VolumeBitmapCommandParameters extends StorageJobCommandParameters {
    private String bitmapName;
    private VdsmImageLocationInfo volInfo;

    public VolumeBitmapCommandParameters() {
    }

    public VolumeBitmapCommandParameters(Guid storagePoolId, VdsmImageLocationInfo volInfo, String bitmapName) {
        setStoragePoolId(storagePoolId);
        setVolInfo(volInfo);
        setBitmapName(bitmapName);
    }

    public String getBitmapName() {
        return bitmapName;
    }

    public void setBitmapName(String bitmapName) {
        this.bitmapName = bitmapName;
    }

    public VdsmImageLocationInfo getVolInfo() {
        return volInfo;
    }

    public void setVolInfo(VdsmImageLocationInfo volInfo) {
        this.volInfo = volInfo;
    }
}
