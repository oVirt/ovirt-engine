package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.compat.Guid;

public class UpgradeStoragePoolVDSCommandParameters extends IrsBaseVDSCommandParameters {
    public UpgradeStoragePoolVDSCommandParameters(Guid storagePoolId, StorageFormatType compatibilityVersion) {
        super(storagePoolId);
        setCompatibilityVersion(compatibilityVersion.getValue());
    }

    public UpgradeStoragePoolVDSCommandParameters() {}

    @Override
    public String toString() {
        return String.format("storagePoolId = %s, poolVersion = %s",
                             getStoragePoolId(), getCompatibilityVersion());
    }
}
