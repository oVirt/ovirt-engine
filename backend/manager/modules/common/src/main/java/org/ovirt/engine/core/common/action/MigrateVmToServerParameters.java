package org.ovirt.engine.core.common.action;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class MigrateVmToServerParameters extends MigrateVmParameters {
    private static final long serialVersionUID = 2378358850714143232L;
    private Guid vdsId;
    private boolean skipScheduling;

    public MigrateVmToServerParameters() {
    }

    public MigrateVmToServerParameters(boolean forceMigration, Guid vmId, Guid serverId, Guid targetClusterId) {
        super(forceMigration, vmId, targetClusterId);
        vdsId = serverId;
    }

    public MigrateVmToServerParameters(boolean forceMigration, Guid vmId, Guid serverId) {
        this(forceMigration, vmId, serverId, null);
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public boolean isSkipScheduling() {
        return skipScheduling;
    }

    public void setSkipScheduling(boolean skipScheduling) {
        this.skipScheduling = skipScheduling;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                vdsId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof MigrateVmToServerParameters)) {
            return false;
        }

        MigrateVmToServerParameters other = (MigrateVmToServerParameters) obj;
        return super.equals(obj)
                && Objects.equals(vdsId, other.vdsId);
    }
}
