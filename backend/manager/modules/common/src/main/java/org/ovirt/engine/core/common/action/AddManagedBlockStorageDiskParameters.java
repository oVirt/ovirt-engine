package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;

public class AddManagedBlockStorageDiskParameters extends AddDiskParameters {
    private static final long serialVersionUID = 6180455140057239311L;

    private boolean shouldPlugDiskToVm;

    public AddManagedBlockStorageDiskParameters() {
    }

    public AddManagedBlockStorageDiskParameters(boolean shouldPlugDiskToVm) {
        this.shouldPlugDiskToVm = shouldPlugDiskToVm;
    }

    public AddManagedBlockStorageDiskParameters(Disk diskInfo,
            boolean shouldPlugDiskToVm) {
        super(diskInfo);
        this.shouldPlugDiskToVm = shouldPlugDiskToVm;
    }

    public AddManagedBlockStorageDiskParameters(DiskVmElement diskVmElement,
            Disk diskInfo, boolean shouldPlugDiskToVm) {
        super(diskVmElement, diskInfo);
        this.shouldPlugDiskToVm = shouldPlugDiskToVm;
    }

    public boolean isShouldPlugDiskToVm() {
        return shouldPlugDiskToVm;
    }

    public void setShouldPlugDiskToVm(boolean shouldPlugDiskToVm) {
        this.shouldPlugDiskToVm = shouldPlugDiskToVm;
    }
}
