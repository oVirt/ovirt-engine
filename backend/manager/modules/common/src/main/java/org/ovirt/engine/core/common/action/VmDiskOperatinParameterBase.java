package org.ovirt.engine.core.common.action;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.compat.Guid;

public class VmDiskOperatinParameterBase extends VmOperationParameterBase {

    private static final long serialVersionUID = 337339450251569362L;

    @Valid
    private DiskImageBase diskInfo;

    public VmDiskOperatinParameterBase() {
    }

    public VmDiskOperatinParameterBase(Guid vmId, DiskImageBase diskInfo) {
        super(vmId);
        setDiskInfo(diskInfo);
    }

    public DiskImageBase getDiskInfo() {
        return diskInfo;
    }

    public void setDiskInfo(DiskImageBase value) {
        diskInfo = value;
    }
}
