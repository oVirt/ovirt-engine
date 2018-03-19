package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.compat.Guid;

public class RemoveMemoryVolumesParameters extends ActionParametersBase {

    private Snapshot snapshot;
    private Guid vmId;
    private boolean forceRemove;

    public RemoveMemoryVolumesParameters(Snapshot snapshot, Guid vmId) {
        this(snapshot, vmId, false);
    }

    public RemoveMemoryVolumesParameters(Snapshot snapshot, Guid vmId, boolean forceRemove) {
        this.snapshot = snapshot;
        this.vmId = vmId;
        this.forceRemove = forceRemove;
    }

    public RemoveMemoryVolumesParameters() {
        this.vmId = Guid.Empty;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
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
