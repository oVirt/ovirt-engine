package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

//TODO MMUCHA: Dear code reviewer! It seems, that this enum is used not only for hot(un)plugging nics, but in other areas as well: HotSetAmountOfMemoryCommand.java, HotSetNumberOfCpusCommand.java which does not have anything in common with (un)plugging nics, am I right? If that's the case, commandType should be removed, or at least renamed.  Please advise.
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
