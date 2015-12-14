package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveMemoryVolumesParameters extends VdcActionParametersBase {
    /** comma-separated string of UUIDs representing the memory volumes */
    private String memoryVolumes;
    private Guid vmId;
    /** In the general case, we remove the memory volumes only if there is only
     *  one snapshot in DB that uses it because we remove the memory before
     *  removing the snapshots from the DB. But in some cases we first remove
     *  the snapshot from the DB and only then remove its memory and in that
     *  case we should remove the memory only if no other snapshot uses it */
    private boolean removeOnlyIfNotUsedAtAll;

    private boolean forceRemove;

    public RemoveMemoryVolumesParameters(String memoryVolumes, Guid vmId) {
        this(memoryVolumes, vmId, false);
    }

    public RemoveMemoryVolumesParameters(String memoryVolumes, Guid vmId, boolean forceRemove) {
        this.memoryVolumes = memoryVolumes;
        this.vmId = vmId;
        this.forceRemove = forceRemove;
    }

    public RemoveMemoryVolumesParameters() {
        this.memoryVolumes = "";
        this.vmId = Guid.Empty;
    }

    public String getMemoryVolumes() {
        return memoryVolumes;
    }

    public void setMemoryVolumes(String memoryVolumes) {
        this.memoryVolumes = memoryVolumes;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public boolean isRemoveOnlyIfNotUsedAtAll() {
        return removeOnlyIfNotUsedAtAll;
    }

    public void setRemoveOnlyIfNotUsedAtAll(boolean removeOnlyIfNotUsedAtAll) {
        this.removeOnlyIfNotUsedAtAll = removeOnlyIfNotUsedAtAll;
    }

    public boolean isForceRemove() {
        return forceRemove;
    }

    public void setForceRemove(boolean forceRemove) {
        this.forceRemove = forceRemove;
    }
}
