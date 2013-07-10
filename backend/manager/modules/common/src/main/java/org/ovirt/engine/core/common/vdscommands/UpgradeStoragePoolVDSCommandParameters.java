package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.compat.Guid;

public class UpgradeStoragePoolVDSCommandParameters extends IrsBaseVDSCommandParameters {
    private String compatibilityVersion;

    public UpgradeStoragePoolVDSCommandParameters(Guid storagePoolId, StorageFormatType compatibilityVersion) {
        super(storagePoolId);
        setCompatibilityVersion(compatibilityVersion.getValue());
    }

    public UpgradeStoragePoolVDSCommandParameters() {}

    public String getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public void setCompatibilityVersion(String value) {
        compatibilityVersion = value;
    }

    @Override
    public String toString() {
        return String.format("storagePoolId = %s, poolVersion = %s",
                             getStoragePoolId(), getCompatibilityVersion());
    }
}
