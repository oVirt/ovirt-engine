package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveMemoryVolumesParameters extends ActionParametersBase {
    /** comma-separated string of UUIDs representing the memory volumes */
    private String memoryVolumes;
    private Guid vmId;
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

    public boolean isForceRemove() {
        return forceRemove;
    }

    public void setForceRemove(boolean forceRemove) {
        this.forceRemove = forceRemove;
    }
}
