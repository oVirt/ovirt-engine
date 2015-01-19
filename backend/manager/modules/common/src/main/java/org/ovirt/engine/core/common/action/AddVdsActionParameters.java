package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.compat.Guid;

public class AddVdsActionParameters extends VdsOperationActionParameters {
    private static final long serialVersionUID = 8452910234577071082L;

    private Guid vdsId;
    private boolean privateAddPending;
    private boolean glusterPeerProbeNeeded;
    private boolean privateAddProvisioned;
    private ExternalHostGroup hostGroup;
    private String hostMac;
    private String discoverName;
    private ExternalComputeResource computeResource;
    private String discoverIp;
    public String getDiscoverIp() { return discoverIp; }
    private List<FenceAgent> fenceAgents;
    public ExternalHostGroup getHostGroup() { return hostGroup; };
    public String getHostMac() { return hostMac; };
    public String getDiscoverName() { return discoverName; };
    public ExternalComputeResource getComputeResource() { return computeResource; }

    public AddVdsActionParameters(VdsStatic vdsStatic, String password) {
        super(vdsStatic, password);
        glusterPeerProbeNeeded = true;
        privateAddProvisioned = false;
    }

    public void initVdsActionParametersForProvision(Guid pid,
                                                    ExternalHostGroup hg,
                                                    ExternalComputeResource cr,
                                                    String mac,
                                                    String discover_name,
                                                    String discover_ip) {
        privateAddProvisioned = true;
        hostMac = mac;
        hostGroup = hg;
        getVdsStaticData().setHostProviderId(pid);
        discoverName = discover_name;
        computeResource = cr;
        discoverIp = discover_ip;
    }

    public boolean getAddProvisioned() {
        return privateAddProvisioned;
    }

    public AddVdsActionParameters() { glusterPeerProbeNeeded = true; }

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

    public boolean getAddPending() {
        return privateAddPending;
    }

    public void setAddPending(boolean value) {
        privateAddPending = value;
    }

    public List<FenceAgent> getFenceAgents() {
        return fenceAgents;
    }

    public void setFenceAgents(List<FenceAgent> fenceAgents) {
        this.fenceAgents = fenceAgents;
    }
}
