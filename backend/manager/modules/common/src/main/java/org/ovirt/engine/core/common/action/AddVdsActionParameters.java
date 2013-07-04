package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;

public class AddVdsActionParameters extends VdsOperationActionParameters {
    private static final long serialVersionUID = 8452910234577071082L;

    public AddVdsActionParameters(VdsStatic vdsStatic, String password) {
        super(vdsStatic, password);
    }

    private Guid vdsId;

    private boolean privateAddPending;

    private boolean glusterPeerProbeNeeded = true;

    public boolean getAddPending() {
        return privateAddPending;
    }

    public void setAddPending(boolean value) {
        privateAddPending = value;
    }

    public AddVdsActionParameters() {
    }

    public void setVdsForUniqueId(Guid serverForUniqueId) {
        this.vdsId = serverForUniqueId;
    }

    public Guid getVdsForUniqueId() {
        return vdsId;
    }

    public void setGlusterPeerProbeNeeded(boolean glusterPeerProbeNeeded) {
        this.glusterPeerProbeNeeded = glusterPeerProbeNeeded;
    }

    public boolean isGlusterPeerProbeNeeded() {
        return this.glusterPeerProbeNeeded;
    }
}
