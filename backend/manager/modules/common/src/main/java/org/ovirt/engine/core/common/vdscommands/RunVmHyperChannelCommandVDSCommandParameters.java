package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class RunVmHyperChannelCommandVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    private String _hcCommand;

    public RunVmHyperChannelCommandVDSCommandParameters(Guid vdsId, Guid vmId, String hcCommand) {
        super(vdsId, vmId);
        _hcCommand = hcCommand;
    }

    public String getHcCommand() {
        return _hcCommand;
    }

    public RunVmHyperChannelCommandVDSCommandParameters() {
    }
}
