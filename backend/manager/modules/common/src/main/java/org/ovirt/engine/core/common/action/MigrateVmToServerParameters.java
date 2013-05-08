package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class MigrateVmToServerParameters extends MigrateVmParameters {
    private static final long serialVersionUID = 2378358850714143232L;
    private Guid vdsId;

    public MigrateVmToServerParameters(boolean forceMigration, Guid vmId, Guid serverId) {
        super(forceMigration, vmId);
        vdsId = serverId;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public MigrateVmToServerParameters() {
    }
}
