package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

public class GetStorageDomainInfoVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    public GetStorageDomainInfoVDSCommandParameters(Guid storagePoolId, Guid storageDomainId) {
        super(storagePoolId, storageDomainId);
    }

    public GetStorageDomainInfoVDSCommandParameters() {
    }
}
