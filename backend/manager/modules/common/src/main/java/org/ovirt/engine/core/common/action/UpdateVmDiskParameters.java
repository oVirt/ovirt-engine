package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;

public class UpdateVmDiskParameters extends VmDiskOperatinParameterBase {
    private static final long serialVersionUID = -272509502118714937L;
    private Guid imageId = Guid.Empty;

    public UpdateVmDiskParameters() {
    }

    public UpdateVmDiskParameters(Guid vmId, Guid imageId, DiskImageBase diskInfo) {
        super(vmId, diskInfo);
        setImageId(imageId);
    }

    public Guid getImageId() {
        return imageId;
    }

    public void setImageId(Guid value) {
        imageId = value;
    }
}
