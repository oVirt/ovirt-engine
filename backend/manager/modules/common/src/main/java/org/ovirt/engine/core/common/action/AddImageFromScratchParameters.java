package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;

public class AddImageFromScratchParameters extends ImagesActionsParametersBase {

    private static final long serialVersionUID = 8249273209551108387L;
    private Guid masterVmId = Guid.Empty;
    private DiskImageBase diskInfo;

    public AddImageFromScratchParameters() {
    }

    public AddImageFromScratchParameters(Guid imageId, Guid vmTemplateId, DiskImageBase diskInfo) {
        super(imageId);
        setMasterVmId(vmTemplateId);
        setDiskInfo(diskInfo);
    }

    public Guid getMasterVmId() {
        return masterVmId;
    }

    public void setMasterVmId(Guid vmId) {
        this.masterVmId = vmId;
    }

    public DiskImageBase getDiskInfo() {
        return diskInfo;
    }

    public void setDiskInfo(DiskImageBase diskInfo) {
        this.diskInfo = diskInfo;
    }
}
