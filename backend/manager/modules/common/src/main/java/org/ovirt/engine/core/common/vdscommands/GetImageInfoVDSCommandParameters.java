package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class GetImageInfoVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    public GetImageInfoVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId, Guid imageId) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
    }

    public GetImageInfoVDSCommandParameters() {
    }
}
