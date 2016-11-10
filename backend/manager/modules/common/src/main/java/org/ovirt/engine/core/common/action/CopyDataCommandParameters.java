package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.compat.Guid;

public class CopyDataCommandParameters extends StorageJobCommandParameters {
    private LocationInfo srcInfo;
    private LocationInfo dstInfo;
    private boolean collapse;

    public CopyDataCommandParameters() {
    }

    public CopyDataCommandParameters(Guid storagePoolId, LocationInfo srcInfo, LocationInfo dstInfo, boolean collapse) {
        this.srcInfo = srcInfo;
        this.dstInfo = dstInfo;
        this.collapse = collapse;
        setStoragePoolId(storagePoolId);
    }

    public LocationInfo getSrcInfo() {
        return srcInfo;
    }

    public void setSrcInfo(LocationInfo srcInfo) {
        this.srcInfo = srcInfo;
    }

    public LocationInfo getDstInfo() {
        return dstInfo;
    }

    public void setDstInfo(LocationInfo dstInfo) {
        this.dstInfo = dstInfo;
    }

    public boolean isCollapse() {
        return collapse;
    }

    public void setCollapse(boolean collapse) {
        this.collapse = collapse;
    }
}
