package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.HttpLocationInfo;
import org.ovirt.engine.core.compat.Guid;

public class UploadImageVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    private HttpLocationInfo uploadInfo;

    public UploadImageVDSCommandParameters(Guid storagePoolId,
                                           Guid storageDomainId,
                                           Guid imageGroupId,
                                           Guid imageId,
                                           HttpLocationInfo uploadInfo) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
        this.uploadInfo = uploadInfo;
    }

    public UploadImageVDSCommandParameters() {
    }

    public HttpLocationInfo getUploadInfo() {
        return uploadInfo;
    }

    public void setUploadInfo(HttpLocationInfo uploadInfo) {
        this.uploadInfo = uploadInfo;
    }
}
