package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class MeasureVolumeParameters extends ActionParametersBase {
    private static final long serialVersionUID = 9149601966505329368L;

    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid imageGroupId;
    private Guid imageId;
    private int dstVolFormat;
    private boolean shouldTeardown;
    private boolean withBacking = true;

    public MeasureVolumeParameters() {
    }

    public MeasureVolumeParameters(Guid storagePoolId,
            Guid storageDomainId,
            Guid imageGroupId,
            Guid imageId,
            int dstVolFormat) {
        this.storagePoolId = storagePoolId;
        this.storageDomainId = storageDomainId;
        this.imageGroupId = imageGroupId;
        this.imageId = imageId;
        this.dstVolFormat = dstVolFormat;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getImageGroupId() {
        return imageGroupId;
    }

    public void setImageGroupId(Guid imageGroupId) {
        this.imageGroupId = imageGroupId;
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid imageId) {
        this.imageId = imageId;
    }

    public boolean isShouldTeardown() {
        return shouldTeardown;
    }

    public void setShouldTeardown(boolean shouldTeardown) {
        this.shouldTeardown = shouldTeardown;
    }

    public int getDstVolFormat() {
        return dstVolFormat;
    }

    public void setDstVolFormat(int dstVolFormat) {
        this.dstVolFormat = dstVolFormat;
    }

    public boolean isWithBacking() {
        return withBacking;
    }

    public void setWithBacking(boolean withBacking) {
        this.withBacking = withBacking;
    }
}
