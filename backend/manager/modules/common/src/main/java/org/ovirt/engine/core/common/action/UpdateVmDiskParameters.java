package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.compat.Guid;

public class UpdateVmDiskParameters extends VmDiskOperationParameterBase {
    private static final long serialVersionUID = -272509502118714937L;
    private Guid diskId;

    public UpdateVmDiskParameters() {
        diskId = Guid.Empty;
    }

    public UpdateVmDiskParameters(Guid vmId, Guid diskId, Disk diskInfo) {
        super(vmId, diskInfo);
        setDiskId(diskId);
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid value) {
        diskId = value;
    }
}
