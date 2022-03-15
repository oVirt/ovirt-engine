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

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("backupId", vmBackup.getId())
                .append("requireConsistency", requireConsistency);
    }
}
