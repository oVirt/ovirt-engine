package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class GetDiskAlignmentParameters extends VdcActionParametersBase {
    private static final long serialVersionUID = -6587274019503875891L;

    private Guid diskId;

    public GetDiskAlignmentParameters() {
    }

    public GetDiskAlignmentParameters(Guid diskId) {
        setDiskId(diskId);
    }

    public Guid getDiskId() {
        return diskId;
    }

    public void setDiskId(Guid diskId) {
        this.diskId = diskId;
    }
}
