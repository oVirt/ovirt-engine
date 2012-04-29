package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public enum PlugAction {
    PLUG(VDSCommandType.HotPlugNic),
    UNPLUG(VDSCommandType.HotUnplugNic);

    private VDSCommandType commandType;

    PlugAction(VDSCommandType commandType) {
        this.commandType= commandType;
    }

    public VDSCommandType getCommandType() {
        return commandType;
    }
}
