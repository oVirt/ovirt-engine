package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.compat.Guid;

public class AmendVolumeCommandParameters extends StorageJobCommandParameters {
    private QcowCompat qcowCompat;
    private LocationInfo volInfo;

    public AmendVolumeCommandParameters() {
    }

    public AmendVolumeCommandParameters(Guid storagePoolId, LocationInfo volInfo, QcowCompat qcowCompat) {
        setStoragePoolId(storagePoolId);
        this.volInfo = volInfo;
        this.qcowCompat = qcowCompat;
    }

    public QcowCompat getQcowCompat() {
        return qcowCompat;
    }

    public void setQcowCompat(QcowCompat qcowCompat) {
        this.qcowCompat = qcowCompat;
    }

    public LocationInfo getVolInfo() {
        return volInfo;
    }

    public void setVolInfo(LocationInfo volInfo) {
        this.volInfo = volInfo;
    }
}
