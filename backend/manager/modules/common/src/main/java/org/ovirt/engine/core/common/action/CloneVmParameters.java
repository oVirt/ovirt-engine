package org.ovirt.engine.core.common.action;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class CloneVmParameters extends AddVmParameters {

    private static final long serialVersionUID = 4851787064680308265L;

    private Guid newVmGuid;

    private String newName;
    private Map<Guid, Map<Guid, DiskImage>> srcToDstChainMap = new HashMap<>();
    private Guid destStorageDomainId;
    private CloneVmStage stage = CloneVmStage.CREATE_VM_SNAPSHOT;
    private Guid sourceSnapshotId;
    private Map<Guid, List<DiskImage>> storageToDisksMap;
    private Collection<VmInterfacesModifyParameters.VnicWithProfile> vnicsWithProfiles;

    private boolean edited;

    public CloneVmParameters() {

    }

    public CloneVmParameters(VM vm, String newName) {
        super(vm);
        this.newName = newName;
    }

    public CloneVmParameters(VM vm, String newName, boolean edited) {
        super(vm);
        this.newName = newName;
        this.edited = edited;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public Guid getNewVmGuid() {
        return newVmGuid;
    }

    public void setNewVmGuid(Guid newVmGuid) {
        this.newVmGuid = newVmGuid;
    }

    public boolean isEdited() {
        return edited;
    }

    public void setEdited(boolean edited) {
        this.edited = edited;
    }

    public Guid getDestStorageDomainId() {
        return destStorageDomainId;
    }

    public void setDestStorageDomainId(Guid destStorageDomainId) {
        this.destStorageDomainId = destStorageDomainId;
    }

    public Map<Guid, Map<Guid, DiskImage>> getSrcToDstChainMap() {
        return srcToDstChainMap;
    }

    public void setSrcToDstChainMap(Map<Guid, Map<Guid, DiskImage>>  srcToDstChainMap) {
        this.srcToDstChainMap = srcToDstChainMap;
    }

    public CloneVmStage getStage() {
        return stage;
    }

    public void setStage(CloneVmStage stage) {
        this.stage = stage;
    }

    public Guid getSourceSnapshotId() {
        return sourceSnapshotId;
    }

    public void setSourceSnapshotId(Guid sourceSnapshotId) {
        this.sourceSnapshotId = sourceSnapshotId;
    }

    public Map<Guid, List<DiskImage>> getStorageToDisksMap() {
        return storageToDisksMap;
    }

    public void setStorageToDisksMap(Map<Guid, List<DiskImage>> storageToDisksMap) {
        this.storageToDisksMap = storageToDisksMap;
    }

    public Collection<VmInterfacesModifyParameters.VnicWithProfile> getVnicsWithProfiles() {
        return vnicsWithProfiles;
    }

    public void setVnicsWithProfiles(Collection<VmInterfacesModifyParameters.VnicWithProfile> vnicsWithProfiles) {
        this.vnicsWithProfiles = vnicsWithProfiles;
    }

    public enum CloneVmStage {
        CREATE_VM_SNAPSHOT,
        COPY_DISKS,
        CLONE_VM,
        CREATE_SNAPSHOTS,
        REMOVE_VM_SNAPSHOT,
        MODIFY_VM_INTERFACES
    }
}
