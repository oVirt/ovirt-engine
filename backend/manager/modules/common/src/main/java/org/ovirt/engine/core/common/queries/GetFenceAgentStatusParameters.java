package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.compat.Guid;

public class GetFenceAgentStatusParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3663389765505476776L;

    private Guid vdsId;
    private String vdsName;
    private String hostName;
    private FenceAgent agent;
    private String pmProxyPreferences;
    private Guid storagePoolId;
    private Guid vdsGroupId;

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

    public Guid getVdsGroupId() {
        return vdsGroupId;
    }

    public void setVdsGroupId(Guid vdsGroupId) {
        this.vdsGroupId = vdsGroupId;
    }

    public String getPmProxyPreferences() {
        return pmProxyPreferences;
    }

    public void setPmProxyPreferences(String pmProxyPreferences) {
        this.pmProxyPreferences = pmProxyPreferences;
    }

}
