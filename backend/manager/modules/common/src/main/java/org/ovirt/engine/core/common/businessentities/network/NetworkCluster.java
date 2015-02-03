package org.ovirt.engine.core.common.businessentities.network;

import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.compat.Guid;

public class NetworkCluster extends IVdcQueryable implements BusinessEntityWithStatus<NetworkClusterId, NetworkStatus> {
    private static final long serialVersionUID = -4900811332744926545L;

    private NetworkClusterId id;
    private NetworkStatus status;
    private boolean display;

    /**
     * A cluster network can be tagged as monitored. Monitored network have implications on automated actions taken on a
     * host during monitoring.
     */
    private boolean required;
    private boolean migration;
    private boolean gluster;

    public NetworkCluster() {
        this(null, null, NetworkStatus.NON_OPERATIONAL, false, true, false, false);
    }

    public NetworkCluster(boolean required) {
        this.required = required;
    }

    public NetworkCluster(Guid clusterId,
            Guid networkId,
            NetworkStatus status,
            boolean display,
            boolean required,
            boolean migration,
            boolean gluster) {
        this(required);
        id = new NetworkClusterId();
        id.setClusterId(clusterId);
        id.setNetworkId(networkId);
        this.status = status;
        this.display = display;
        this.migration = migration;
        this.gluster = gluster;
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

    @Override
    public NetworkStatus getStatus() {
        return this.status;
    }

    @Override
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

    public boolean isMigration() {
        return migration;
    }

    public void setMigration(boolean migration) {
        this.migration = migration;
    }

    public boolean isGluster() {
        return gluster;
    }

    public void setGluster(boolean gluster) {
        this.gluster = gluster;
    }

    @Override
    public Object getQueryableId() {
        return getId();
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
                .append(", migration=")
                .append(isMigration())
                .append(", gluster=")
                .append(isGluster())
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
        result = prime * result + (migration ? 1231 : 1237);
        result = prime * result + (gluster ? 1231 : 1237);
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
        if (migration != other.migration) {
            return false;
        }
        if (gluster != other.gluster) {
            return false;
        }
        return true;
    }

}
