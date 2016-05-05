package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.compat.Guid;

public class UpdateVmDiskParameters extends VmDiskOperationParameterBase {
    private static final long serialVersionUID = -272509502118714937L;

    public UpdateVmDiskParameters() {
    }

    public UpdateVmDiskParameters(Guid vmId, Disk diskInfo) {
        super(vmId, diskInfo);
    }
}
