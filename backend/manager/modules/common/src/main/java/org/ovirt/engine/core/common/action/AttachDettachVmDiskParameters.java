package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class AttachDettachVmDiskParameters extends VmDiskOperationParameterBase {

    private static final long serialVersionUID = 5640716432695539552L;
    private boolean isPlugUnPlug;

    public AttachDettachVmDiskParameters(Guid vmId, Guid diskId) {
        super(vmId, null);
        setEntityId(diskId);
        this.isPlugUnPlug = false;
    }

    public AttachDettachVmDiskParameters(Guid vmId, Guid diskId, boolean isPlugUnPlug) {
        super(vmId, null);
        setEntityId(diskId);
        this.isPlugUnPlug = isPlugUnPlug;
    }

    public void setPlugUnPlug(boolean isPlugUnPlug) {
        this.isPlugUnPlug = isPlugUnPlug;
    }

    public boolean isPlugUnPlug() {
        return isPlugUnPlug;
    }

}
