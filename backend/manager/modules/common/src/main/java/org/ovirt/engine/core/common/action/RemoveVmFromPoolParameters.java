package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveVmFromPoolParameters extends VmPoolParametersBase {

    private static final long serialVersionUID = -9051384517712295806L;

    private Guid vmId;
    private boolean removePoolUponDetachAllVMs;
    private boolean updatePrestartedVms;

    public RemoveVmFromPoolParameters() {
        this(Guid.Empty, false, true);
    }

    public RemoveVmFromPoolParameters(Guid vmId, boolean removePoolUponDetachAllVMs, boolean updatePrestartedVms) {
        super(Guid.Empty);
        this.vmId = vmId;
        this.removePoolUponDetachAllVMs = removePoolUponDetachAllVMs;
        this.updatePrestartedVms = updatePrestartedVms;
    }

    public Guid getVmId() {
        return vmId;
    }

    public boolean isRemovePoolUponDetachAllVMs() {
        return removePoolUponDetachAllVMs;
    }

    public boolean isUpdatePrestartedVms() {
        return updatePrestartedVms;
    }

}
