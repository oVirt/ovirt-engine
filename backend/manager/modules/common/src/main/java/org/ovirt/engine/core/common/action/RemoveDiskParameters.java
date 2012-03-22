package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveDiskParameters extends VdcActionParametersBase {

    private static final long serialVersionUID = -9133825126638788603L;
    private Guid storageDomainId;
    private boolean forceDelete;

    public RemoveDiskParameters() {
    }

    public RemoveDiskParameters(Guid diskId) {
        setEntityId(diskId);
    }

    public RemoveDiskParameters(Guid diskId, Guid storageDomainId) {
        this(diskId);
        this.storageDomainId = storageDomainId;
    }

    public void setStorageDomainId(Guid storageId) {
        this.storageDomainId = storageId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setForceDelete(boolean forceDelete) {
        this.forceDelete = forceDelete;
    }

    public boolean getForceDelete() {
        return forceDelete;
    }

}
