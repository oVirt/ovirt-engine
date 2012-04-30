package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class ShutdownVdsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private boolean _reboot;

    public ShutdownVdsVDSCommandParameters(Guid vdsId, boolean reboot) {
        super(vdsId);
        _reboot = reboot;
    }

    public boolean getReboot() {
        return _reboot;
    }

    public ShutdownVdsVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, reboot=%s", super.toString(), getReboot());
    }
}
