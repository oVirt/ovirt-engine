package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmsByDiskGuidParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -6977853398143134633L;
    private Guid diskGuid;

    public GetVmsByDiskGuidParameters() {
        this(Guid.Empty);
    }

    public GetVmsByDiskGuidParameters(Guid diskGuid) {
        this.diskGuid = diskGuid;
    }

    public Guid getDiskGuid() {
        return diskGuid;
    }
}
