package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class ProcessOvfUpdateForStoragePoolParameters extends StoragePoolParametersBase {
    // Relevant for dc level in which OVF on any domain isn't supported.
    private boolean updateStorage = true;

    public ProcessOvfUpdateForStoragePoolParameters() {
        super();
    }

    public ProcessOvfUpdateForStoragePoolParameters(Guid storagePoolId) {
        super(storagePoolId);
    }

    public boolean isUpdateStorage() {
        return updateStorage;
    }

    public void setUpdateStorage(boolean updateStorage) {
        this.updateStorage = updateStorage;
    }

}
