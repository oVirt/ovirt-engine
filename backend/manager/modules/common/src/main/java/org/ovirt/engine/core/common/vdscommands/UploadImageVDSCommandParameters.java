package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.compat.Guid;

public class UploadImageVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    private LocationInfo uploadInfo;

    public UploadImageVDSCommandParameters(Guid storagePoolId,
                                           Guid storageDomainId,
                                           Guid imageGroupId,
                                           Guid imageId,
                                           LocationInfo uploadInfo) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
        this.uploadInfo = uploadInfo;
    }

    public LocationInfo getUploadInfo() {
        return uploadInfo;
    }

    public void setUploadInfo(LocationInfo uploadInfo) {
        this.uploadInfo = uploadInfo;
    }
}
