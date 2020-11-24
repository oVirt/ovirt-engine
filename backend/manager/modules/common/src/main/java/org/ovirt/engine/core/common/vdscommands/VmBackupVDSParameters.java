package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VmBackupVDSParameters extends VdsIdVDSCommandParametersBase {

    private VmBackup vmBackup;
    private boolean requireConsistency;

    public VmBackupVDSParameters() {
    }

    public VmBackupVDSParameters(Guid vdsId, VmBackup vmBackup) {
        this(vdsId, vmBackup, false);
    }

    public VmBackupVDSParameters(Guid vdsId, VmBackup vmBackup, boolean requireConsistency) {
        super(vdsId);
        this.vmBackup = vmBackup;
        this.requireConsistency = requireConsistency;
    }

    public VmBackup getVmBackup() {
        return vmBackup;
    }

    public void setVmBackup(VmBackup value) {
        this.vmBackup = value;
    }

    public boolean isRequireConsistency() {
        return requireConsistency;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("backupId", vmBackup.getId())
                .append("requireConsistency", requireConsistency);
    }
}
