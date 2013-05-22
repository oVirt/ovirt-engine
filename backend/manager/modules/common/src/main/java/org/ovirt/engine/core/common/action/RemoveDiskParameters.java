package org.ovirt.engine.core.common.action;
import org.ovirt.engine.core.compat.Guid;

public class RemoveDiskParameters extends VdcActionParametersBase {

    private static final long serialVersionUID = -3691440035048144457L;
    private Guid diskId;
    private Guid storageDomainId;
    private boolean forceDelete;

    public RemoveDiskParameters() {
    }

    public RemoveDiskParameters(Guid diskId) {
        this.diskId = diskId;
    }

    public RemoveDiskParameters(Guid diskId, Guid storageDomainId) {
        this(diskId);
        this.storageDomainId = storageDomainId;
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
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
