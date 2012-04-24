package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;

public class UpdateVmDiskParameters extends VmDiskOperatinParameterBase {
    private static final long serialVersionUID = -272509502118714937L;
    private Guid diskId = Guid.Empty;

    public UpdateVmDiskParameters() {
    }

    public UpdateVmDiskParameters(Guid vmId, Guid diskId, DiskImageBase diskInfo) {
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
