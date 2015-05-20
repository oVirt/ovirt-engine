package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

public class RepoImageModel extends EntityModel<RepoImage> {

    private String diskImageAlias;

    public RepoImageModel(RepoImage repoImage) {
        super(repoImage);
    }

    public String getDiskImageAlias() {
        if (diskImageAlias == null) {
            diskImageAlias = RepoImage.getRepoImageAlias(
                    StorageConstants.GLANCE_DISK_ALIAS_PREFIX, getEntity().getRepoImageId());
        }
        return diskImageAlias;
    }

    public void setDiskImageAlias(String diskImageAlias) {
        this.diskImageAlias = diskImageAlias;
    }
}
