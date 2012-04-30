package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;

public class GetVGInfoVDSCommandParameters extends RemoveVGVDSCommandParameters {
    public GetVGInfoVDSCommandParameters(Guid vdsId, String vgId) {
        super(vdsId, vgId);
    }

    public GetVGInfoVDSCommandParameters() {
    }
}
