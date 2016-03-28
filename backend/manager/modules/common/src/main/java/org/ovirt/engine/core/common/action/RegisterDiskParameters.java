package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class RegisterDiskParameters extends ImagesActionsParametersBase {

    private static final long serialVersionUID = -1939500674856778636L;

    private DiskImage diskImage;

    private boolean refreshFromStorage;

    public RegisterDiskParameters() {
    }

    public RegisterDiskParameters(DiskImage diskImage, Guid storageDomainId) {
        super(diskImage.getId());
        this.diskImage = diskImage;
        setStorageDomainId(storageDomainId);
    }

    public DiskImage getDiskImage() {
        return diskImage;
    }

    public boolean isRefreshFromStorage() {
        return refreshFromStorage;
    }

    public void setRefreshFromStorage(boolean refreshFromStorage) {
        this.refreshFromStorage = refreshFromStorage;
    }
}
