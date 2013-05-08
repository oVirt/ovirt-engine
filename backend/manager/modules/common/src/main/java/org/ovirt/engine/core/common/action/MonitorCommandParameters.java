package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class MonitorCommandParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = 3727810581213783933L;
    private String _command;

    public MonitorCommandParameters(Guid vmId, String command) {
        super(vmId);
        _command = command;
    }

    public String getCommand() {
        return _command;
    }

    public MonitorCommandParameters() {
    }
}
