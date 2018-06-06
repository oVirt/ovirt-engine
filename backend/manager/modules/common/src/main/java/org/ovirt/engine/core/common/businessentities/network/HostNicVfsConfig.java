package org.ovirt.engine.core.common.businessentities.network;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class HostNicVfsConfig implements Queryable, BusinessEntity<Guid> {

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

    /**
     * The actual number of free virtual functions are on the nic
     */
    private int numOfFreeVfs;

    private boolean allNetworksAllowed;

    private Set<Guid> networks;

    private Set<String> networkLabels;

    public HostNicVfsConfig() {
        networks = new HashSet<>();
        networkLabels = new HashSet<>();
    }

    public HostNicVfsConfig(HostNicVfsConfig vfsConfig) {
        setId(vfsConfig.getId());
        setNicId(vfsConfig.getNicId());
        setMaxNumOfVfs(vfsConfig.getMaxNumOfVfs());
        setNumOfVfs(vfsConfig.getNumOfVfs());
        setNumOfFreeVfs(vfsConfig.getNumOfFreeVfs());

        setAllNetworksAllowed(vfsConfig.isAllNetworksAllowed());

        Set<Guid> networks = new HashSet<>();
        networks.addAll(vfsConfig.getNetworks());
        setNetworks(networks);

        Set<String> labels = new HashSet<>();
        labels.addAll(vfsConfig.getNetworkLabels());
        setNetworkLabels(labels);
    }

    public HostNicVfsConfig(Guid id, Guid nicId, boolean allNetworksAllowed) {
        setId(id);
        setNicId(nicId);
        setAllNetworksAllowed(allNetworksAllowed);
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

    public int getNumOfFreeVfs() {
        return numOfFreeVfs;
    }

    public void setNumOfFreeVfs(int numOfFreeVfs) {
        this.numOfFreeVfs = numOfFreeVfs;
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
        return Objects.hashCode(nicId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof HostNicVfsConfig)) {
            return false;
        }
        HostNicVfsConfig other = (HostNicVfsConfig) obj;
        return Objects.equals(nicId, other.nicId);
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
                .append("NumOfFreeVfs", getNumOfFreeVfs())
                .build();
    }

}
