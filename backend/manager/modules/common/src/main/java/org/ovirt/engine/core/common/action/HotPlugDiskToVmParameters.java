package org.ovirt.engine.core.common.action;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.compat.Guid;

public class HotPlugDiskToVmParameters extends VmDiskOperationParameterBase {

    private static final long serialVersionUID = -1003552157459962546L;

    @NotNull
    private Guid diskId;

    public HotPlugDiskToVmParameters(Guid vmId, Guid diskId) {
        super(vmId, null);
        setDiskId(diskId);
    }

    public HotPlugDiskToVmParameters() {
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
    }
}
