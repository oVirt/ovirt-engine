package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class IrsBaseVDSCommandParameters extends VDSParametersBase {
    public IrsBaseVDSCommandParameters(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
    }

    private Guid privateStoragePoolId = Guid.Empty;

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    public void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    private boolean privateIgnoreFailoverLimit;

    public boolean getIgnoreFailoverLimit() {
        return privateIgnoreFailoverLimit;
    }

    public void setIgnoreFailoverLimit(boolean value) {
        privateIgnoreFailoverLimit = value;
    }

    private String privateCompatibilityVersion;

    public String getCompatibilityVersion() {
        return privateCompatibilityVersion;
    }

    public void setCompatibilityVersion(String value) {
        privateCompatibilityVersion = value;
    }

    public IrsBaseVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("storagePoolId = %s, ignoreFailoverLimit = %s, compatabilityVersion = %s",
                getStoragePoolId(), getIgnoreFailoverLimit(), getCompatibilityVersion());
    }
}
