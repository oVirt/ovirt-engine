package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class NetworkCluster implements Queryable, BusinessEntityWithStatus<NetworkClusterId, NetworkStatus> {
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
    private boolean management;
    private boolean defaultRoute;
    private boolean gluster;

    public NetworkCluster() {
        this(null, null, NetworkStatus.NON_OPERATIONAL, false, true, false, false, false, false);
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
            boolean management,
            boolean gluster,
            boolean defaultRoute) {
        this(required);
        id = new NetworkClusterId();
        id.setClusterId(clusterId);
        id.setNetworkId(networkId);
        this.status = status;
        this.display = display;
        this.migration = migration;
        this.management = management;
        this.gluster = gluster;
        this.defaultRoute = defaultRoute;
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

    public boolean isManagement() {
        return management;
    }

    public void setManagement(boolean management) {
        this.management = management;
    }

    public boolean isGluster() {
        return gluster;
    }

    public void setGluster(boolean gluster) {
        this.gluster = gluster;
    }

    public boolean isDefaultRoute() {
        return defaultRoute;
    }

    public void setDefaultRoute(boolean defaultRoute) {
        this.defaultRoute = defaultRoute;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("status", getStatus())
                .append("display", isDisplay())
                .append("required", isRequired())
                .append("migration", isMigration())
                .append("management", isManagement())
                .append("gluster", isGluster())
                .append("defaultRoute", isDefaultRoute())
                .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                display,
                status,
                id,
                required,
                migration,
                management,
                gluster,
                defaultRoute
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NetworkCluster)) {
            return false;
        }

        NetworkCluster other = (NetworkCluster) obj;
        return display == other.display
                && status == other.status
                && Objects.equals(id, other.id)
                && required == other.required
                && migration == other.migration
                && management == other.management
                && gluster == other.gluster
                && defaultRoute == other.defaultRoute;
    }

}
