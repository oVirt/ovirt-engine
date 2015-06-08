package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.Guid;

public class RemoveCinderDiskParameters extends RemoveImageParameters {

    private CinderDisk removedVolume;
    private boolean faultTolerant;
    private boolean lockVm = true;

    public RemoveCinderDiskParameters() {
    }

    public RemoveCinderDiskParameters(Guid diskId) {
        // We use disk id instead of image id.
        super(diskId);
    }

    public boolean isFaultTolerant() {
        return faultTolerant;
    }

    public void setFaultTolerant(boolean faultTolerant) {
        this.faultTolerant = faultTolerant;
    }

    public CinderDisk getRemovedVolume() {
        return removedVolume;
    }

    public void setRemovedVolume(CinderDisk removedVolume) {
        this.removedVolume = removedVolume;
    }

    public Guid getDiskId() {
        return getImageId();
    }

    public boolean isLockVm() {
        return lockVm;
    }

    public void setLockVm(boolean lockVm) {
        this.lockVm = lockVm;
    }
}
