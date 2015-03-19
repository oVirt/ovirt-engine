package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
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

    private List<Guid> networks;

    private List<String> networkLabels;

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

    public List<Guid> getNetworks() {
        return networks;
    }

    public void setNetworks(List<Guid> networks) {
        this.networks = networks;
    }

    public List<String> getNetworkLabels() {
        return networkLabels;
    }

    public void setNetworkLabels(List<String> networkLabels) {
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
        StringBuilder builder = new StringBuilder();
        builder.append(" {id=")
                .append(getId())
                .append(" {nicId=")
                .append(getNicId())
                .append(" {allNetworksAllowed=")
                .append(isAllNetworksAllowed())
                .append(" {maxNumOfVfs=")
                .append(getMaxNumOfVfs())
                .append(" {numOfVfs=")
                .append(getNumOfVfs())
                .append(" {networks=")
                .append(getNetworks())
                .append(" {networkLabels=")
                .append(getNetworkLabels())
                .append("}");
        return builder.toString();
    }

}
