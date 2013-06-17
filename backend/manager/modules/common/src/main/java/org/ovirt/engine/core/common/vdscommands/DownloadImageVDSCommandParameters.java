package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.LocationInfo;
import org.ovirt.engine.core.compat.Guid;

public class DownloadImageVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    private LocationInfo downloadInfo;

    public DownloadImageVDSCommandParameters(Guid storagePoolId,
                                             Guid storageDomainId,
                                             Guid imageGroupId,
                                             Guid imageId,
                                             LocationInfo downloadInfo) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
        this.downloadInfo = downloadInfo;
    }

    public LocationInfo getDownloadInfo() {
        return downloadInfo;
    }

    public void setDownloadInfo(LocationInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }
}
