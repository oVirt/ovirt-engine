package org.ovirt.engine.core.common.action;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class CloneVmParameters extends AddVmParameters {

    private Guid newVmGuid;

    private String newName;
    private Map<Guid, Map<Guid, DiskImage>> srcToDstChainMap = new HashMap<>();
    private Guid destStorageDomainId;
    private CloneVmStage stage = CloneVmStage.COPY_DISKS;

    public CloneVmParameters() {

    }

    public CloneVmParameters(VM vm, String newName) {
        super(vm);
        this.newName = newName;
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

    public enum CloneVmStage {
        COPY_DISKS,
        CREATE_SNAPSHOTS
    }
}
