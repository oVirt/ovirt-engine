package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;

public class UpdateDiskParameters extends VmDiskOperationParameterBase {

    private static final long serialVersionUID = -3961620663417567556L;

    private List<Phase> diskUpdatePhases = new ArrayList<>();

    public UpdateDiskParameters() {
    }

    public UpdateDiskParameters(Disk diskInfo) {
        super(diskInfo);
    }

    public UpdateDiskParameters(DiskVmElement diskVmElement, Disk diskInfo) {
        super(diskVmElement, diskInfo);
    }

    public List<Phase> getDiskUpdatePhases() {
        return diskUpdatePhases;
    }

    public void setDiskUpdatePhases(List<Phase> diskUpdatePhases) {
        this.diskUpdatePhases = diskUpdatePhases;
    }

    public enum Phase {
        AMEND_DISK,
        EXTEND_DISK,
        UPDATE_DISK
    }
}
