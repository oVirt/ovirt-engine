package org.ovirt.engine.core.common.vdscommands;

import java.util.Map;

import org.ovirt.engine.core.compat.Guid;


public abstract class GetDiskAlignmentVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    public GetDiskAlignmentVDSCommandParameters(Guid vdsId, Guid vmId) {
        super(vdsId, vmId);
    }

    public GetDiskAlignmentVDSCommandParameters() {
    }

    public abstract Map<String, String> getDriveSpecs();
}
