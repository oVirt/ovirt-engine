package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetAllDisksByVmIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1277592901283660498L;

    public GetAllDisksByVmIdParameters(Guid vmId) {
        _vmId = vmId;
    }

    private Guid _vmId = new Guid();

    public Guid getVmId() {
        return _vmId;
    }

    public GetAllDisksByVmIdParameters() {
    }
}
