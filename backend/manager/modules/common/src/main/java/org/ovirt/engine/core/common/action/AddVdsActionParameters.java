package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;

public class AddVdsActionParameters extends VdsOperationActionParameters {
    private static final long serialVersionUID = 8452910234577071082L;

    public AddVdsActionParameters(VdsStatic vdsStatic, String password) {
        super(vdsStatic, password);
        glusterPeerProbeNeeded = true;
    }

    private Guid vdsId;

    private boolean privateAddPending;

    private boolean glusterPeerProbeNeeded;
    private Guid providerId;
    private String networkMappings;

    public boolean getAddPending() {
        return privateAddPending;
    }

    public void setAddPending(boolean value) {
        privateAddPending = value;
    }

    public AddVdsActionParameters() {
        glusterPeerProbeNeeded = true;
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

    public Guid getProviderId() {
        return providerId;
    }

    public void setProvider(Guid provider) {
        this.providerId = provider;
    }

    public String getNetworkMappings() {
        return networkMappings;
    }

    public void setNetworkMappings(String networkMappings) {
        this.networkMappings = networkMappings;
    }
}
