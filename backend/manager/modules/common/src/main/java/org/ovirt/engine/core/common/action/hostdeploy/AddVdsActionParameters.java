package org.ovirt.engine.core.common.action.hostdeploy;

import java.util.List;

import org.ovirt.engine.core.common.action.VdsOperationActionParameters;
import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.VdsStatic;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.compat.Guid;


public class AddVdsActionParameters extends VdsOperationActionParameters {
    private static final long serialVersionUID = 8452910234577071082L;

    private Guid hostId;
    private boolean pending;
    private boolean glusterPeerProbeNeeded;
    private boolean provisioned;
    private ExternalHostGroup hostGroup;
    private String hostMac;
    private String discoverName;
    private ExternalComputeResource computeResource;
    private String discoverIp;
    private List<FenceAgent> fenceAgents;

    public AddVdsActionParameters() {
        glusterPeerProbeNeeded = true;
    }

    public AddVdsActionParameters(VdsStatic vdsStatic, String password) {
        super(vdsStatic, password);
        glusterPeerProbeNeeded = true;
    }

    public void initVdsActionParametersForProvision(Guid providerId,
            ExternalHostGroup hostGroup,
            ExternalComputeResource computeResource,
            String hostMac,
            String discoverName,
            String discoverIp) {
        provisioned = true;
        this.hostMac = hostMac;
        this.hostGroup = hostGroup;
        this.getVdsStaticData().setHostProviderId(providerId);
        this.discoverName = discoverName;
        this.computeResource = computeResource;
        this.discoverIp = discoverIp;
    }

    public boolean isProvisioned() {
        return provisioned;
    }

    public void setVdsForUniqueId(Guid serverForUniqueId) {
        this.hostId = serverForUniqueId;
    }

    public Guid getVdsForUniqueId() {
        return hostId;
    }

    public void setGlusterPeerProbeNeeded(boolean glusterPeerProbeNeeded) {
        this.glusterPeerProbeNeeded = glusterPeerProbeNeeded;
    }

    public boolean isGlusterPeerProbeNeeded() {
        return glusterPeerProbeNeeded;
    }

    public boolean isPending() {
        return pending;
    }

    public void setPending(boolean pending) {
        this.pending = pending;
    }

    public List<FenceAgent> getFenceAgents() {
        return fenceAgents;
    }

    public void setFenceAgents(List<FenceAgent> fenceAgents) {
        this.fenceAgents = fenceAgents;
    }

    public String getDiscoverIp() {
        return discoverIp;
    }

    public ExternalHostGroup getHostGroup() {
        return hostGroup;
    }

    public String getHostMac() {
        return hostMac;
    }

    public String getDiscoverName() {
        return discoverName;
    }

    public ExternalComputeResource getComputeResource() {
        return computeResource;
    }
}
