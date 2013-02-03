package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DiskImage;

public class RegisterDiskParameters extends ImagesActionsParametersBase {

    private static final long serialVersionUID = -1939500674856778636L;

    private DiskImage diskImage;

    public RegisterDiskParameters(DiskImage diskImage) {
        super(diskImage.getId());
        this.diskImage = diskImage;
    }

    public DiskImage getDiskImage() {
        return diskImage;
    }
}
