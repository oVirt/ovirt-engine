package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class ReconcileVolumeChainVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {

    private ReconcileVolumeChainVDSCommandParameters() { }

    public ReconcileVolumeChainVDSCommandParameters(
            Guid storagePoolId,
            Guid storageDomainId,
            Guid imageGroupId,
            Guid imageId) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
    }
}
