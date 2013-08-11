package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class IrsBaseVDSCommandParameters extends VDSParametersBase {
    private Guid storagePoolId;
    private boolean ignoreFailoverLimit;

    public IrsBaseVDSCommandParameters(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
    }

    public IrsBaseVDSCommandParameters() {
        storagePoolId = Guid.Empty;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid value) {
        storagePoolId = value;
    }

    public boolean getIgnoreFailoverLimit() {
        return ignoreFailoverLimit;
    }

    public void setIgnoreFailoverLimit(boolean value) {
        ignoreFailoverLimit = value;
    }

    @Override
    public String toString() {
        return String.format("storagePoolId = %s, ignoreFailoverLimit = %s",
                getStoragePoolId(), getIgnoreFailoverLimit());
    }
}
