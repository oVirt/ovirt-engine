package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class SwitchMasterStorageDomainCommandParameters extends StorageDomainParametersBase {
    private static final long serialVersionUID = 8697545661482740042L;

    private Guid currentMasterStorageDomainId;

    public SwitchMasterStorageDomainCommandParameters() {
    }

    public SwitchMasterStorageDomainCommandParameters(Guid storagePoolId, Guid storageDomainId) {
        super(storagePoolId, storageDomainId);
    }

    public void setCurrentMasterStorageDomainId(Guid currentMasterStorageDomainId) {
        this.currentMasterStorageDomainId = currentMasterStorageDomainId;
    }

    public Guid getCurrentMasterStorageDomainId() {
        return currentMasterStorageDomainId;
    }
}
