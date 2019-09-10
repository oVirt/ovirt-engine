package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;

public class VmDiskOperationParameterBase extends VmOperationParameterBase {

    private static final long serialVersionUID = 337339450251569362L;

    @Valid
    private Disk diskInfo;

    @Valid
    private DiskVmElement diskVmElement;

    public VmDiskOperationParameterBase() {
    }

    public VmDiskOperationParameterBase(DiskVmElement diskVmElement) {
        this(diskVmElement, null);
    }

    public VmDiskOperationParameterBase(DiskVmElement diskVmElement, Disk diskInfo) {
        super(diskVmElement.getVmId());
        setDiskVmElement(diskVmElement);
        setDiskInfo(diskInfo);
    }

    public VmDiskOperationParameterBase(Disk diskInfo) {
        setDiskInfo(diskInfo);
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
