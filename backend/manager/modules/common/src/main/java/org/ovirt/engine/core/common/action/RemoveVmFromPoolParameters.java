package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveVmFromPoolParameters extends VmPoolParametersBase {
    private static final long serialVersionUID = -9051384517712295806L;
    private Guid vmId;
    private boolean removePoolUponDetachAllVMs;

    public RemoveVmFromPoolParameters(Guid vmId) {
        this(vmId, false);
    }

    public RemoveVmFromPoolParameters(Guid vmId, boolean removePoolUponDetachAllVMs) {
        super(Guid.Empty);
        this.vmId = vmId;
        this.removePoolUponDetachAllVMs = removePoolUponDetachAllVMs;
    }

    public RemoveVmFromPoolParameters() {
        vmId = Guid.Empty;
    }

    public Guid getVmId() {
        return vmId;
    }

    public boolean isRemovePoolUponDetachAllVMs() {
        return removePoolUponDetachAllVMs;
    }
}
