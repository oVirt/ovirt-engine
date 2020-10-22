package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class CopyDataCommandParameters extends StorageJobCommandParameters {
    private LocationInfo srcInfo;
    private LocationInfo dstInfo;
    private boolean collapse;
    private boolean live;
    private List<DiskImage> destImages = new ArrayList<>();
    private boolean copyBitmaps;

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

    public List<DiskImage> getDestImages() {
        return destImages;
    }

    public void setDestImages(List<DiskImage> destImages) {
        this.destImages = destImages;
    }

    public boolean isCopyBitmaps() {
        return copyBitmaps;
    }

    public void setCopyBitmaps(boolean copyBitmaps) {
        this.copyBitmaps = copyBitmaps;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }
}
