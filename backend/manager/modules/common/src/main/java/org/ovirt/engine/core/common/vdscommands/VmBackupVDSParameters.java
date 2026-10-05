package org.ovirt.engine.core.common.vdscommands;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VmBackupVDSParameters extends VdsIdVDSCommandParametersBase {

    private VmBackup vmBackup;
    private boolean requireConsistency;
    // Map between the backed-up disk ID to the created scratch disk image
    // and the path to it after the scratch disk was prepared.
    private Map<Guid, ScratchDiskInfo> scratchDisksMap;
    // Whether the redefine of the checkpoints should validate them against
    // qemu's live state. False is used to redefine a checkpoint with a broken
    // bitmap, so it can be deleted afterwards (the delete removes the bitmap).
    private boolean validateCheckpoints = true;

    public VmBackupVDSParameters() {
    }

    public VmBackupVDSParameters(Guid vdsId, VmBackup vmBackup) {
        this(vdsId, vmBackup, false, new HashMap<>());
    }

    public VmBackupVDSParameters(Guid vdsId,
            VmBackup vmBackup,
            boolean requireConsistency,
            Map<Guid, ScratchDiskInfo> scratchDisksMap) {
        super(vdsId);
        this.vmBackup = vmBackup;
        this.requireConsistency = requireConsistency;
        this.scratchDisksMap = scratchDisksMap;
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

    public Map<Guid, ScratchDiskInfo> getScratchDisksMap() {
        return scratchDisksMap;
    }

    public void setScratchDisksMap(Map<Guid, ScratchDiskInfo> scratchDisksMap) {
        this.scratchDisksMap = scratchDisksMap;
    }

    public boolean isValidateCheckpoints() {
        return validateCheckpoints;
    }

    public void setValidateCheckpoints(boolean validateCheckpoints) {
        this.validateCheckpoints = validateCheckpoints;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("backupId", vmBackup.getId())
                .append("requireConsistency", requireConsistency);
    }
}
