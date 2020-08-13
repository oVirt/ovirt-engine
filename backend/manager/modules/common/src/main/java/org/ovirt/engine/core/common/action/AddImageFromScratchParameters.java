package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class AddImageFromScratchParameters extends CreateSnapshotParameters {

    private static final long serialVersionUID = 8249273209551108387L;
    private Guid masterVmId;
    private DiskImage diskInfo;
    private boolean shouldRemainIllegalOnFailedExecution;

    public AddImageFromScratchParameters() {
        masterVmId = Guid.Empty;
    }

    public AddImageFromScratchParameters(Guid imageId, Guid vmTemplateId, DiskImage diskInfo) {
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

    public DiskImage getDiskInfo() {
        return diskInfo;
    }

    public void setDiskInfo(DiskImage diskInfo) {
        this.diskInfo = diskInfo;
    }

    public boolean isShouldRemainIllegalOnFailedExecution() {
        return shouldRemainIllegalOnFailedExecution;
    }

    public void setShouldRemainIllegalOnFailedExecution(boolean shouldRemainIllegalOnFailedExecution) {
        this.shouldRemainIllegalOnFailedExecution = shouldRemainIllegalOnFailedExecution;
    }
}
