package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.compat.Guid;

public class VmCheckpointVDSParameters extends VdsAndVmIDVDSParametersBase {

    private VmCheckpoint checkpoint;

    public VmCheckpointVDSParameters() {
    }

    public VmCheckpointVDSParameters(Guid vdsId, Guid vmId, VmCheckpoint checkpoint) {
        super(vdsId, vmId);
        this.checkpoint = checkpoint;
    }

    public VmCheckpoint getCheckpoint() {
        return checkpoint;
    }

    public void setCheckpoints(VmCheckpoint checkpoint) {
        this.checkpoint = checkpoint;
    }
}
