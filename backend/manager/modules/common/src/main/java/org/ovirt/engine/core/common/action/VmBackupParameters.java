package org.ovirt.engine.core.common.action;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

public class VmBackupParameters extends VmOperationParameterBase implements Serializable {
    private static final long serialVersionUID = -3821623510049174551L;

    @Valid
    @NotNull
    private VmBackup vmBackup;
    private boolean backupInitiated;
    private Guid toCheckpointId;
    private boolean requireConsistency;
    // Map between the backed-up disk ID to the created scratch disk image
    // and the path to it after the scratch disk prepared.
    private Map<Guid, Pair<DiskImage, String>> scratchDisksMap = new HashMap<>();

    public VmBackupParameters() {
    }

    public VmBackupParameters(VmBackup vmBackup) {
        this(vmBackup, false);
    }

    public VmBackupParameters(VmBackup vmBackup, boolean requireConsistency) {
        this.vmBackup = vmBackup;
        this.requireConsistency = requireConsistency;
    }

    public VmBackup getVmBackup() {
        return vmBackup;
    }

    public void setVmBackup(VmBackup value) {
        vmBackup = value;
    }

    public boolean isBackupInitiated() {
        return backupInitiated;
    }

    public void setBackupInitiated(boolean backupInitiated) {
        this.backupInitiated = backupInitiated;
    }

    public Guid getToCheckpointId() {
        return toCheckpointId;
    }

    public void setToCheckpointId(Guid toCheckpointId) {
        this.toCheckpointId = toCheckpointId;
    }

    public boolean isRequireConsistency() {
        return requireConsistency;
    }

    public Map<Guid, Pair<DiskImage, String>> getScratchDisksMap() {
        return scratchDisksMap;
    }

    public void setScratchDisksMap(Map<Guid, Pair<DiskImage, String>> scratchDisksMap) {
        this.scratchDisksMap = scratchDisksMap;
    }

    @Override
    public Guid getVmId() {
        return getVmBackup() != null ? getVmBackup().getVmId() : null;
    }
}
