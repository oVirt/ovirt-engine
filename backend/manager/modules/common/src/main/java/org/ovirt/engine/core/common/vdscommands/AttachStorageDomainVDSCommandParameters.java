package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

public class AttachStorageDomainVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    public AttachStorageDomainVDSCommandParameters(Guid storagePoolId, Guid storageDomainId) {
        super(storagePoolId, storageDomainId);
    }

    public AttachStorageDomainVDSCommandParameters() {
    }
}
