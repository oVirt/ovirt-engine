package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.compat.Guid;

public class VmDiskOperationParameterBase extends VmOperationParameterBase {

    private static final long serialVersionUID = 337339450251569362L;

    @Valid
    private Disk diskInfo;
    private Guid snapshotId;
    private Guid vdsId;

    public VmDiskOperationParameterBase() {
    }

    public VmDiskOperationParameterBase(Guid vmId, Disk diskInfo) {
        super(vmId);
        setDiskInfo(diskInfo);
    }

    public Disk getDiskInfo() {
        return diskInfo;
    }

    public void setDiskInfo(Disk value) {
        diskInfo = value;
    }

    public Guid getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Guid snapshotId) {
        this.snapshotId = snapshotId;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
    }
}
