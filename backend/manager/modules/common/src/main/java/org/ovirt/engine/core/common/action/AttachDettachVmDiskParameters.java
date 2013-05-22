package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.compat.Guid;

public class AttachDettachVmDiskParameters extends VmDiskOperationParameterBase {

    private static final long serialVersionUID = 5640716432695539552L;
    private boolean isPlugUnPlug;

    public AttachDettachVmDiskParameters(Guid vmId, Guid diskId, boolean isPlugUnPlug) {
        super(vmId, null);
        setEntityInfo(new EntityInfo(VdcObjectType.Disk, diskId));
        this.isPlugUnPlug = isPlugUnPlug;
    }

    public AttachDettachVmDiskParameters(Guid vmId, Guid diskId) {
        this(vmId, diskId, false);
    }

    public void setPlugUnPlug(boolean isPlugUnPlug) {
        this.isPlugUnPlug = isPlugUnPlug;
    }

    public boolean isPlugUnPlug() {
        return isPlugUnPlug;
    }

}
