package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ChangeDiskVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private String _diskPath;

    public ChangeDiskVDSCommandParameters(Guid vdsId, Guid vmId, String diskPath) {
        super(vdsId, vmId);
        _diskPath = diskPath;
    }

    public String getDiskPath() {
        return _diskPath;
    }

    public ChangeDiskVDSCommandParameters() {
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("diskPath", getDiskPath());
    }
}
