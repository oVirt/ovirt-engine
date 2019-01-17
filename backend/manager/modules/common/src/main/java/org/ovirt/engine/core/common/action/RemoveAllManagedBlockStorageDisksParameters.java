package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.compat.Guid;

public class RemoveAllManagedBlockStorageDisksParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = -8526126549458582857L;

    private Guid storageDomainId;

    private List<ManagedBlockStorageDisk> managedBlockDisks;

    public RemoveAllManagedBlockStorageDisksParameters() {
    }

    public RemoveAllManagedBlockStorageDisksParameters(Guid vmId, List<ManagedBlockStorageDisk> managedBlockDisks) {
        super(vmId);
        this.managedBlockDisks = managedBlockDisks;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public List<ManagedBlockStorageDisk> getManagedBlockDisks() {
        return managedBlockDisks;
    }

    public void setManagedBlockDisks(List<ManagedBlockStorageDisk> managedBlockDisks) {
        this.managedBlockDisks = managedBlockDisks;
    }
}
