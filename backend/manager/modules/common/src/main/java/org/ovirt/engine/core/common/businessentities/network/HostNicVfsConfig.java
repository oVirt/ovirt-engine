package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class HostNicVfsConfig extends IVdcQueryable implements Serializable, BusinessEntity<Guid> {

    private static final long serialVersionUID = 2762897334386252961L;

    private Guid id;

    private Guid nicId;

    /**
     * The theoretical maximum number of virtual functions can be on the nic
     */
    private int maxNumOfVfs;

    /**
     * The actual number of virtual functions are on the nic
     */
    private int numOfVfs;

    private boolean allNetworksAllowed;

    private Set<Guid> networks;

    private Set<String> networkLabels;

    public HostNicVfsConfig() {
        networks = new HashSet<>();
        networkLabels = new HashSet<>();
    }

    public Guid getNicId() {
        return nicId;
    }

    public void setNicId(Guid nicId) {
        this.nicId = nicId;
    }

    public int getMaxNumOfVfs() {
        return maxNumOfVfs;
    }

    public void setMaxNumOfVfs(int maxNumOfVfs) {
        this.maxNumOfVfs = maxNumOfVfs;
    }

    public int getNumOfVfs() {
        return numOfVfs;
    }

    public void setNumOfVfs(int numOfVfs) {
        this.numOfVfs = numOfVfs;
    }

    public boolean isAllNetworksAllowed() {
        return allNetworksAllowed;
    }

    public void setAllNetworksAllowed(boolean allNetworksAllowed) {
        this.allNetworksAllowed = allNetworksAllowed;
    }

    public Set<Guid> getNetworks() {
        return networks;
    }

    public void setNetworks(Set<Guid> networks) {
        this.networks = networks;
    }

    public Set<String> getNetworkLabels() {
        return networkLabels;
    }

    public void setNetworkLabels(Set<String> networkLabels) {
        this.networkLabels = networkLabels;
    }

    @Override
    public Guid getId() {
        return this.id;
    }

    @Override
    public void setId(Guid value) {
        this.id = value;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(nicId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        HostNicVfsConfig other = (HostNicVfsConfig) obj;
        return Objects.equals(nicId, other.getNicId());
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("nicId", getNicId())
                .append("allNetworksAllowed", isAllNetworksAllowed())
                .append("maxNumOfVfs", getMaxNumOfVfs())
                .append("numOfVfs", getNumOfVfs())
                .append("networks", getNetworks())
                .append("networkLabels", getNetworkLabels())
                .build();
    }

}
