package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class MigrateVmToServerParameters extends MigrateVmParameters {
    private static final long serialVersionUID = 2378358850714143232L;
    private Guid vdsId;

    public MigrateVmToServerParameters() {
    }

    public MigrateVmToServerParameters(boolean forceMigration, Guid vmId, Guid serverId, Guid targetVdsGroupId) {
        super(forceMigration, vmId, targetVdsGroupId);
        vdsId = serverId;
    }

    public MigrateVmToServerParameters(boolean forceMigration, Guid vmId, Guid serverId) {
        this(forceMigration, vmId, serverId, null);
    }

    public Guid getVdsId() {
        return vdsId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((vdsId == null) ? 0 : vdsId.hashCode());
        return result;
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
                && ObjectUtils.objectsEqual(vdsId, other.vdsId);
    }
}
