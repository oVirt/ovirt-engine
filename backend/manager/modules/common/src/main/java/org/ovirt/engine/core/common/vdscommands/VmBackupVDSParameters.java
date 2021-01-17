package org.ovirt.engine.core.common.vdscommands;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VmBackupVDSParameters extends VdsIdVDSCommandParametersBase {

    private VmBackup vmBackup;
    private boolean requireConsistency;
    // Map between the backed-up disk ID to the created scratch disk image path.
    private Map<Guid, String> scratchDisksMap;

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

    public Map<Guid, String> getScratchDisksMap() {
        return scratchDisksMap;
    }

    public void setScratchDisksMap(Map<Guid, String> scratchDisksMap) {
        this.scratchDisksMap = scratchDisksMap;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("backupId", vmBackup.getId())
                .append("requireConsistency", requireConsistency);
    }
}
