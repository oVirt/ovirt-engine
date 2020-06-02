package org.ovirt.engine.core.common.action;

import java.io.Serializable;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.compat.Guid;

public class VmCheckpointParameters extends VmOperationParameterBase implements Serializable {
    private static final long serialVersionUID = -6814474714106693881L;

    @Valid
    @NotNull
    private VmCheckpoint vmCheckpoint;

    public VmCheckpointParameters() {
    }

    public VmCheckpointParameters(VmCheckpoint vmCheckpoint) {
        this.vmCheckpoint = vmCheckpoint;
    }

    public VmCheckpoint getVmCheckpoint() {
        return vmCheckpoint;
    }

    public void setVmCheckpoint(VmCheckpoint vmCheckpoint) {
        this.vmCheckpoint = vmCheckpoint;
    }

    @Override
    public Guid getVmId() {
        return getVmCheckpoint() != null ? getVmCheckpoint().getVmId() : null;
    }
}
