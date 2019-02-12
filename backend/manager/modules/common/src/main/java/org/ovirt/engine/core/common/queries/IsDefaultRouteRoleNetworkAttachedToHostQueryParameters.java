package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class IsDefaultRouteRoleNetworkAttachedToHostQueryParameters extends QueryParametersBase {
    private Guid clusterId;
    private Guid hostId;

    public IsDefaultRouteRoleNetworkAttachedToHostQueryParameters() {
    }

    public IsDefaultRouteRoleNetworkAttachedToHostQueryParameters(Guid clusterId, Guid hostId) {
        this.clusterId = clusterId;
        this.hostId = hostId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public Guid getHostId() {
        return hostId;
    }

    public void setHostId(Guid hostId) {
        this.hostId = hostId;
    }
}
