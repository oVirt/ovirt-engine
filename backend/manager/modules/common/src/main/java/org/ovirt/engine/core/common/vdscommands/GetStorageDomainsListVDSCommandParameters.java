package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

public class GetStorageDomainsListVDSCommandParameters extends IrsBaseVDSCommandParameters {
    public GetStorageDomainsListVDSCommandParameters(Guid storagePoolId) {
        super(storagePoolId);
    }

    public GetStorageDomainsListVDSCommandParameters() {
    }
}
