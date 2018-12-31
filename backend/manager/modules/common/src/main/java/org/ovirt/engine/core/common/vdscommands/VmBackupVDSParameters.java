package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VmBackupVDSParameters extends VdsIdVDSCommandParametersBase {

    private VmBackup vmBackup;

    public VmBackupVDSParameters() {
    }

    public VmBackupVDSParameters(Guid vdsId, VmBackup vmBackup) {
        super(vdsId);
        this.vmBackup = vmBackup;
    }

    public VmBackup getVmBackup() {
        return vmBackup;
    }

    public void setVmBackup(VmBackup value) {
        this.vmBackup = value;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("backupId", vmBackup.getId());
    }
}
