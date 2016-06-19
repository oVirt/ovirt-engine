package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class UpdateVmVersionParameters extends VmManagementParametersBase {

    private Guid vmPoolId;
    /** The ID of disk operator of one of the disks the VM had before the update */
    private Guid previousDiskOperatorAuthzPrincipalDbId;
    private Guid newTemplateVersion;
    /** The new VM latest property. if null -> uses the original, source VM, latest property. */
    private Boolean useLatestVersion;
    /** Whether or not to lock the VM during this operation */
    private boolean lockVm = true;

    public UpdateVmVersionParameters() {
    }

    public UpdateVmVersionParameters(Guid vmId) {
        this(vmId, null, null); // use latest template version, based on the vm existing latest field
    }

    public UpdateVmVersionParameters(Guid vmId, Guid newTemplateVersion, Boolean useLatestVersion) {
        super();
        setVmId(vmId);
        setNewTemplateVersion(newTemplateVersion);
        setUseLatestVersion(useLatestVersion);
    }


    public Guid getNewTemplateVersion() {
        return newTemplateVersion;
    }

    public void setNewTemplateVersion(Guid newTemplateVersion) {
        this.newTemplateVersion = newTemplateVersion;
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

    public Boolean getUseLatestVersion() {
        return useLatestVersion;
    }

    public void setUseLatestVersion(Boolean useLatestVersion) {
        this.useLatestVersion = useLatestVersion;
    }

    public boolean isLockVm() {
        return lockVm;
    }

    public void setLockVm(boolean lockVm) {
        this.lockVm = lockVm;
    }
}
