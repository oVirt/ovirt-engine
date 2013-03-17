package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class GetStoragePoolInfoVDSCommandParameters extends GetStorageDomainsListVDSCommandParameters {
    public GetStoragePoolInfoVDSCommandParameters(Guid storagePoolId) {
        super(storagePoolId);
    }

    public GetStoragePoolInfoVDSCommandParameters() {
    }
}
