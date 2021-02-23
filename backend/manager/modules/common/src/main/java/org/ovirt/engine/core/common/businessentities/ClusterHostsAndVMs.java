package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class ClusterHostsAndVMs implements Queryable {

    private static final long serialVersionUID = -5395392502656683858L;

    private Guid clusterId;
    private int hosts;
    private int hostsWithUpdateAvailable;
    private int vms;

    @Override
    public Object getQueryableId() {
        return getClusterId();
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public int getHosts() {
        return hosts;
    }

    public void setHosts(int hosts) {
        this.hosts = hosts;
    }

    public int getHostsWithUpdateAvailable() {
      return hostsWithUpdateAvailable;
    }

    public void setHostsWithUpdateAvailable(int hostsWithUpdateAvailable) {
      this.hostsWithUpdateAvailable = hostsWithUpdateAvailable;
    }

    public int getVms() {
        return vms;
    }

    public void setVms(int vms) {
        this.vms = vms;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                hosts,
                clusterId,
                vms,
                hostsWithUpdateAvailable
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ClusterHostsAndVMs)) {
            return false;
        }
        ClusterHostsAndVMs other = (ClusterHostsAndVMs) obj;
        return hosts == other.hosts
                && Objects.equals(clusterId, other.clusterId)
                && vms == other.vms
                && hostsWithUpdateAvailable == other.hostsWithUpdateAvailable;
    }

}
