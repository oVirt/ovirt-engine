package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.compat.Guid;

public class AttachDetachVmDiskParameters extends VmDiskOperationParameterBase {

    private static final long serialVersionUID = 5640716432695539552L;
    private boolean isPlugUnPlug;
    private boolean isReadOnly;

    public AttachDetachVmDiskParameters() {
    }

    public AttachDetachVmDiskParameters(Guid vmId, Guid diskId) {
        this(vmId, diskId, true, false);
    }

    public AttachDetachVmDiskParameters(Guid vmId, Guid diskId, boolean isPlugUnPlug, boolean isReadOnly) {
        super(vmId, null);
        setEntityInfo(new EntityInfo(VdcObjectType.Disk, diskId));
        this.isPlugUnPlug = isPlugUnPlug;
        this.isReadOnly = isReadOnly;
    }

    public void setPlugUnPlug(boolean isPlugUnPlug) {
        this.isPlugUnPlug = isPlugUnPlug;
    }

    public boolean isPlugUnPlug() {
        return isPlugUnPlug;
    }

    public boolean isReadOnly() {
        return isReadOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.isReadOnly = readOnly;
    }
}
