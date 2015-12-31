package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class AddUnmanagedVmsParameters extends VdsActionParameters {
    private static final long serialVersionUID = 4577666096624926622L;

    private List<Guid> vmIds;

    public AddUnmanagedVmsParameters() {
    }

    public AddUnmanagedVmsParameters(Guid hostId, List<Guid> vmIds) {
        super(hostId);
        setVmIds(vmIds);
    }

    public List<Guid> getVmIds() {
        return vmIds;
    }

    public void setVmIds(List<Guid> vmIds) {
        this.vmIds = vmIds;
    }
}
