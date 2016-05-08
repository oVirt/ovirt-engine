package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.compat.Guid;

public class VmDiskOperationParameterBase extends VmOperationParameterBase {

    private static final long serialVersionUID = 337339450251569362L;

    @Valid
    private Disk diskInfo;
    private DiskVmElement diskVmElement;

    public VmDiskOperationParameterBase() {
    }

    public VmDiskOperationParameterBase(Guid vmId, Disk diskInfo) {
        super(vmId);
        setDiskInfo(diskInfo);
    }

    public VmDiskOperationParameterBase(DiskVmElement diskVmElement) {
        super(diskVmElement.getVmId());
        setDiskVmElement(diskVmElement);
    }

    public Disk getDiskInfo() {
        return diskInfo;
    }

    public void setDiskInfo(Disk value) {
        diskInfo = value;
    }

    public DiskVmElement getDiskVmElement() {
        return diskVmElement;
    }

    public void setDiskVmElement(DiskVmElement diskVmElement) {
        this.diskVmElement = diskVmElement;
    }
}
