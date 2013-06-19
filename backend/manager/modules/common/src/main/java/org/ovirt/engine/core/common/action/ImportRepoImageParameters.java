package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class ImportRepoImageParameters extends ImagesActionsParametersBase {

    private static final long serialVersionUID = 8168949491104775480L;

    private String sourceRepoImageId;

    private DiskImage diskImage;

    private Guid sourceStorageDomainId;

    public String getSourceRepoImageId() {
        return sourceRepoImageId;
    }

    public void setSourceRepoImageId(String sourceRepoImageId) {
        this.sourceRepoImageId = sourceRepoImageId;
    }

    public Guid getSourceStorageDomainId() {
        return sourceStorageDomainId;
    }

    public void setSourceStorageDomainId(Guid sourceStorageDomainId) {
        this.sourceStorageDomainId = sourceStorageDomainId;
    }

    public DiskImage getDiskImage() {
        return diskImage;
    }

    public void setDiskImage(DiskImage diskImage) {
        this.diskImage = diskImage;
    }

}
