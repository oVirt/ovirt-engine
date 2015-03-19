package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.FenceAgent;
import org.ovirt.engine.core.compat.Guid;

public class GetFenceAgentStatusParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -3663389765505476776L;

    private Guid _vds_id;
    private FenceAgent agent;
    private String pmProxyPreferences;
    private Guid vdsGroupId;

    public GetFenceAgentStatusParameters() {
        _storagePoolId = Guid.Empty;
    }

    public FenceAgent getAgent() {
        return agent;
    }

    public void setAgent(FenceAgent agent) {
        this.agent = agent;
    }

    public Guid getVdsId() {
        return _vds_id;
    }

    public void setVdsId(Guid value) {
        _vds_id = value;
    }

    private Guid _storagePoolId;

    public Guid getStoragePoolId() {
        return _storagePoolId;
    }

    public void setStoragePoolId(Guid value) {
        _storagePoolId = value;
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
