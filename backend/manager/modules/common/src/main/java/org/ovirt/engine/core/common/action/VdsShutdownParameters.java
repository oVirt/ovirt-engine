package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;

public class VdsShutdownParameters extends VdsActionParameters {
    private static final long serialVersionUID = 6589404824277164961L;
    private boolean privateReboot;

    public boolean getReboot() {
        return privateReboot;
    }

    private void setReboot(boolean value) {
        privateReboot = value;
    }

    public VdsShutdownParameters(Guid vdsId, boolean reboot) {
        super(vdsId);
        setReboot(reboot);
    }

    public VdsShutdownParameters() {
    }
}
