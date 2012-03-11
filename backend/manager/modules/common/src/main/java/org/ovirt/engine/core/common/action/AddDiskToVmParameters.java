package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;

public class AddDiskToVmParameters extends VmDiskOperatinParameterBase {
    private static final long serialVersionUID = -7832310521101821905L;
    private Guid privateVmSnapshotId = Guid.Empty;
    private Guid privateStorageDomainId = Guid.Empty;

    public AddDiskToVmParameters() {
    }

    public AddDiskToVmParameters(Guid vmId, DiskImageBase diskInfo) {
        super(vmId, diskInfo);
    }

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    public Guid getVmSnapshotId() {
        return privateVmSnapshotId;
    }

    public void setVmSnapshotId(Guid value) {
        privateVmSnapshotId = value;
    }
}
