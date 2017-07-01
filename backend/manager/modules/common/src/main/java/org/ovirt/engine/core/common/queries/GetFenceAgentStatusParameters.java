package org.ovirt.engine.core.common.queries;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.businessentities.pm.FenceProxySourceType;
import org.ovirt.engine.core.compat.Guid;

public class GetFenceAgentStatusParameters extends QueryParametersBase {
    private static final long serialVersionUID = -3663389765505476776L;

    private Guid vdsId;
    private String vdsName;
    private String hostName;
    private FenceAgent agent;
    private List<FenceProxySourceType> fenceProxySources;
    private Guid storagePoolId;
    private Guid clusterId;

    public GetFenceAgentStatusParameters() {
        storagePoolId = Guid.Empty;
    }

    public FenceAgent getAgent() {
        return agent;
    }

    public void setAgent(FenceAgent agent) {
        this.agent = agent;
    }

    public Guid getVdsId() {
        return vdsId;
    }

    public void setVdsId(Guid value) {
        vdsId = value;
    }

    public String getVdsName() {
        return vdsName;
    }

    public void setVdsName(String vdsName) {
        this.vdsName = vdsName;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid value) {
        storagePoolId = value;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public List<FenceProxySourceType> getFenceProxySources() {
        return fenceProxySources;
    }

    public void setFenceProxySources(List<FenceProxySourceType> fenceProxySources) {
        this.fenceProxySources = fenceProxySources;
    }
}
