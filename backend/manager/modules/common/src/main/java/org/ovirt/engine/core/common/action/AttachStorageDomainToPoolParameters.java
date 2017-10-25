package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class AttachStorageDomainToPoolParameters extends StorageDomainPoolParametersBase {
    private boolean activate = true;

    public AttachStorageDomainToPoolParameters() {
    }

    public AttachStorageDomainToPoolParameters(Guid storageDomainId, Guid storagePoolId) {
        super(storageDomainId, storagePoolId);
    }

    public boolean getActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }
}
