package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetHostsForStorageOperationParameters extends StoragePoolQueryParametersBase {

    private boolean localFsOnly;

    public GetHostsForStorageOperationParameters(Guid storagePoolId, boolean localFsOnly) {
        super(storagePoolId);
        this.localFsOnly = localFsOnly;
    }

    public boolean isLocalFsOnly() {
        return localFsOnly;
    }
}
