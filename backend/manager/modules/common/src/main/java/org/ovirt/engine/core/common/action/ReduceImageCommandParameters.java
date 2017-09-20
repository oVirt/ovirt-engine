package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class ReduceImageCommandParameters extends StorageDomainParametersBase {

    private static final long serialVersionUID = -8310991446163142084L;
    private Guid spmId;
    private Guid runningVdsId;
    private Guid imageGroupId;
    private Guid imageId;
    private DiskImage activeDiskImage;
    private boolean allowActive;

    public ReduceImageCommandParameters() {
    }

    public ReduceImageCommandParameters(Guid spmId,
            Guid runningVdsId,
            Guid storagePoolId,
            Guid storageDomainId,
            Guid imageGroupId,
            Guid imageId,
            DiskImage activeDiskImage,
            boolean allowActive) {
        super(storagePoolId, storageDomainId);
        this.spmId = spmId;
        this.runningVdsId = runningVdsId;
        this.imageGroupId = imageGroupId;
        this.imageId = imageId;
        this.activeDiskImage = activeDiskImage;
        this.allowActive = allowActive;
    }

    public Guid getSpmId() {
        return spmId;
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

    public boolean isAllowActive() {
        return allowActive;
    }

    public boolean isVmRunningOnSpm() {
        return spmId.equals(this.runningVdsId);
    }

    public Guid getRunningVdsId() {
        return runningVdsId;
    }

    public DiskImage getActiveDiskImage() {
        return activeDiskImage;
    }
}
