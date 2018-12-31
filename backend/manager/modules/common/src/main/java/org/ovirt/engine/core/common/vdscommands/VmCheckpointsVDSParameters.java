package org.ovirt.engine.core.common.vdscommands;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.compat.Guid;

public class VmCheckpointsVDSParameters extends VdsAndVmIDVDSParametersBase {

    private List<VmCheckpoint> checkpoints;

    public VmCheckpointsVDSParameters() {
    }

    public VmCheckpointsVDSParameters(Guid vdsId, Guid vmId, List<VmCheckpoint> checkpoints) {
        super(vdsId, vmId);
        this.checkpoints = checkpoints;
    }

    public List<VmCheckpoint> getCheckpoints() {
        return checkpoints;
    }

    public void setCheckpoints(List<VmCheckpoint> checkpoints) {
        this.checkpoints = checkpoints;
    }

    public List<Guid> getCheckpointsIds() {
        return checkpoints != null ?
                checkpoints.stream().map(VmCheckpoint::getId).collect(Collectors.toList()) :
                Collections.EMPTY_LIST;
    }
}
