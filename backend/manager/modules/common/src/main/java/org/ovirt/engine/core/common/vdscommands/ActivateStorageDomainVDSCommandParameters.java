package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class ActivateStorageDomainVDSCommandParameters extends StorageDomainIdParametersBase {
    public ActivateStorageDomainVDSCommandParameters(Guid storagePoolId, Guid storageDomainId) {
        super(storagePoolId);
        setStorageDomainId(storageDomainId);
    }

    public ActivateStorageDomainVDSCommandParameters() {
    }
}
