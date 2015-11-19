package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class NetworkClusterId implements Serializable {

    private static final long serialVersionUID = 4662794069699019632L;

    public Guid clusterId;

    public Guid networkId;

    public NetworkClusterId() {
    }

    public NetworkClusterId(Guid clusterId, Guid networkId) {
        this.clusterId = clusterId;
        this.networkId = networkId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public Guid getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Guid networkId) {
        this.networkId = networkId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                clusterId,
                networkId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NetworkClusterId)) {
            return false;
        }
        NetworkClusterId other = (NetworkClusterId) obj;
        return Objects.equals(clusterId, other.clusterId)
                && Objects.equals(networkId, other.networkId);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("clusterId", getClusterId())
                .append("networkId", getNetworkId())
                .build();
    }
}
