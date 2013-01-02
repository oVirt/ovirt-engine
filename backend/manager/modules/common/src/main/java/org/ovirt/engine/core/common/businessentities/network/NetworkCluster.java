package org.ovirt.engine.core.common.businessentities.network;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "network_cluster")
@TypeDef(name = "guid", typeClass = GuidType.class)
@NamedQueries(value = { @NamedQuery(name = "delete_network_cluster",
                                    query = "delete from network_cluster n where n.clusterId = :cluster_id and n.networkId = :network_id") })
public class NetworkCluster extends IVdcQueryable implements BusinessEntity<NetworkClusterId> {
    private static final long serialVersionUID = -4900811332744926545L;

    private NetworkClusterId id = new NetworkClusterId();

    @Column(name = "status")
    private NetworkStatus status = NetworkStatus.NON_OPERATIONAL;

    @Column(name = "is_display")
    private boolean display;

    /**
     * A cluster network can be tagged as monitored. Monitored network have implications on automated actions taken on a
     * host during monitoring.
     */
    private boolean required = true;

    public NetworkCluster() {
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("NetworkCluster {id=")
                .append(getId())
                .append(", status=")
                .append(getStatus())
                .append(", display=")
                .append(isDisplay())
                .append(", required=")
                .append(isRequired())
                .append("}");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (display ? 1231 : 1237);
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + (required ? 11 : 13);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NetworkCluster other = (NetworkCluster) obj;
        if (display != other.display)
            return false;
        if (status == null) {
            if (other.status != null)
                return false;
        } else if (!status.equals(other.status))
            return false;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (required != other.required) {
            return false;
        }
        return true;
    }


    public NetworkCluster(Guid clusterId, Guid networkId, NetworkStatus status, boolean display, boolean required) {
        id.setClusterId(clusterId);
        id.setNetworkId(networkId);
        this.status = status;
        this.display = display;
        this.required = required;
    }

    @Override
    public NetworkClusterId getId() {
        return id;
    }

    @Override
    public void setId(NetworkClusterId id) {
        this.id = id;
    }

    public Guid getClusterId() {
        return id.getClusterId();
    }

    public void setClusterId(Guid value) {
        id.setClusterId(value);
    }

    public Guid getNetworkId() {
        return id.getNetworkId();
    }

    public void setNetworkId(Guid value) {
        id.setNetworkId(value);
    }

    public NetworkStatus getStatus() {
        return this.status;
    }

    public void setStatus(NetworkStatus value) {
        this.status = value;
    }

    public boolean isDisplay() {
        return this.display;
    }

    public void setDisplay(boolean value) {
        this.display = value;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }
}
