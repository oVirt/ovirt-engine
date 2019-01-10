package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;

public class ExtendManagedBlockStorageDiskSizeParameters extends VmDiskOperationParameterBase {

    private static final long serialVersionUID = 5002115812827491485L;

    private Guid storageDomainId;

    public ExtendManagedBlockStorageDiskSizeParameters() {
    }

    public ExtendManagedBlockStorageDiskSizeParameters(DiskVmElement diskVmElement, Disk diskInfo) {
        super(diskVmElement, diskInfo);
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }
}
