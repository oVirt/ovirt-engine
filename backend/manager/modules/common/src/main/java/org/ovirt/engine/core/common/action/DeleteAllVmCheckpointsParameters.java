package org.ovirt.engine.core.common.action;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class DeleteAllVmCheckpointsParameters extends VmOperationParameterBase {

    private static final long serialVersionUID = 3234203100268643132L;

    @Valid
    @NotNull
    private List<DiskImage> diskImages;
    private int completedDisksCount;

    public DeleteAllVmCheckpointsParameters() {
    }

    public DeleteAllVmCheckpointsParameters(Guid vmId, List<DiskImage> diskImages) {
        super(vmId);
        this.diskImages = diskImages;
        completedDisksCount = 0;
    }

    public List<DiskImage> getDiskImages() {
        return diskImages;
    }

    public void setDiskImages(List<DiskImage> diskImages) {
        this.diskImages = diskImages;
    }

    public int getCompletedDisksCount() {
        return completedDisksCount;
    }

    public void setCompletedDisksCount(int completedDisksCount) {
        this.completedDisksCount = completedDisksCount;
    }
}
