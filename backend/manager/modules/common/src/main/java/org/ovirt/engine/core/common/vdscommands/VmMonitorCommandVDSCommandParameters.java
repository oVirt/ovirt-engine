package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class VmMonitorCommandVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private String _command;

    public VmMonitorCommandVDSCommandParameters(Guid vdsId, Guid vmId, String command) {
        super(vdsId, vmId);
        _command = command;
    }

    public String getCommand() {
        return _command;
    }

    public VmMonitorCommandVDSCommandParameters() {
    }
}
