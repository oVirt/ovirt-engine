package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;

public class AddDiskToVmParameters extends VmDiskOperatinParameterBase {
    private static final long serialVersionUID = -7832310521101821905L;
    private Guid vmSnapshotId = Guid.Empty;
    private Guid storageDomainId = Guid.Empty;

    public AddDiskToVmParameters() {
    }

    public AddDiskToVmParameters(Guid vmId, DiskImageBase diskInfo) {
        super(vmId, diskInfo);
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        storageDomainId = value;
    }

    public Guid getVmSnapshotId() {
        return vmSnapshotId;
    }

    public void setVmSnapshotId(Guid value) {
        vmSnapshotId = value;
    }
}
