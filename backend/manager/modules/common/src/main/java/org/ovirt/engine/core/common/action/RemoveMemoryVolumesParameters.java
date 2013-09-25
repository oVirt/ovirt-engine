package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveMemoryVolumesParameters extends VdcActionParametersBase {
    /** comma-separated string of UUIDs representing the memory volumes */
    private String memoryVolumes;
    private Guid vmId;

    public RemoveMemoryVolumesParameters(String memoryVolumes, Guid vmId) {
        this.memoryVolumes = memoryVolumes;
        this.vmId = vmId;
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
}
