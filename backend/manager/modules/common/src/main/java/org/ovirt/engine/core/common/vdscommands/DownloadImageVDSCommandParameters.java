package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.HttpLocationInfo;
import org.ovirt.engine.core.compat.Guid;

public class DownloadImageVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    private HttpLocationInfo downloadInfo;

    public DownloadImageVDSCommandParameters(Guid storagePoolId,
                                             Guid storageDomainId,
                                             Guid imageGroupId,
                                             Guid imageId,
                                             HttpLocationInfo downloadInfo) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
        this.downloadInfo = downloadInfo;
    }

    public DownloadImageVDSCommandParameters() {
    }

    public HttpLocationInfo getDownloadInfo() {
        return downloadInfo;
    }

    public void setDownloadInfo(HttpLocationInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }
}
