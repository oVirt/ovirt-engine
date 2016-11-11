package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public enum PlugAction {
    PLUG(VDSCommandType.HotPlugNic),
    UNPLUG(VDSCommandType.HotUnplugNic);

    private VDSCommandType vNicVdsCommandType;

    PlugAction(VDSCommandType vNicVdsCommandType) {
        this.vNicVdsCommandType = vNicVdsCommandType;
    }

    /**
     * @return {@link VDSCommandType} to perform {@code this} plug action.
     */
    public VDSCommandType getvNicVdsCommandType() {
        return vNicVdsCommandType;
    }
}
