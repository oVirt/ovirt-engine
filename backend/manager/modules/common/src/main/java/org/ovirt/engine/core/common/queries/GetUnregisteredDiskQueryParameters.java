package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetUnregisteredDiskQueryParameters extends GetUnregisteredDisksQueryParameters {

    private static final long serialVersionUID = 4444477909827451298L;

    private Guid diskId;

    public GetUnregisteredDiskQueryParameters() {
    }

    public GetUnregisteredDiskQueryParameters(Guid diskId, Guid storageDomainId, Guid storagePoolId) {
        super(storageDomainId, storagePoolId);
        this.diskId = diskId;
    }

    public Guid getDiskId() {
        return diskId;
    }
}
