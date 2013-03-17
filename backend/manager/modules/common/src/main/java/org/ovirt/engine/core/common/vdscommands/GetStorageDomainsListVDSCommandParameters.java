package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class GetStorageDomainsListVDSCommandParameters extends IrsBaseVDSCommandParameters {
    public GetStorageDomainsListVDSCommandParameters(Guid storagePoolId) {
        super(storagePoolId);
    }

    public GetStorageDomainsListVDSCommandParameters() {
    }
}
