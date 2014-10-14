package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class UpdateVmVersionParameters extends VmManagementParametersBase {

    private Guid vmPoolId;
    /** The ID of disk operator of one of the disks the VM had before the update */
    private Guid previousDiskOperatorAuthzPrincipalDbId;

    public UpdateVmVersionParameters() {
    }

    public UpdateVmVersionParameters(Guid vmId) {
        super();
        setVmId(vmId);
    }

    public Guid getVmPoolId() {
        return vmPoolId;
    }

    public void setVmPoolId(Guid vmPoolId) {
        this.vmPoolId = vmPoolId;
    }

    public Guid getPreviousDiskOperatorAuthzPrincipalDbId() {
        return previousDiskOperatorAuthzPrincipalDbId;
    }

    public void setPreviousDiskOperatorAuthzPrincipalDbId(Guid previousDiskOperatorAuthzPrincipalDbId) {
        this.previousDiskOperatorAuthzPrincipalDbId = previousDiskOperatorAuthzPrincipalDbId;
    }
}
