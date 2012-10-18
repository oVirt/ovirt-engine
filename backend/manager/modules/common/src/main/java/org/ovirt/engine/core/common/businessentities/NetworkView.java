package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Version;

public class NetworkView extends IVdcQueryable implements Serializable {

    private static final long serialVersionUID = 7541192304006710467L;

    private Network network;
    private String storagePoolName;
    private Version compatibilityVersion;

    @Override
    public Object getQueryableId() {
        return network.getQueryableId();
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network network) {
        this.network = network;
    }

    public String getStoragePoolName() {
        return storagePoolName;
    }

    public void setStoragePoolName(String storagePoolName) {
        this.storagePoolName = storagePoolName;
    }

    public Version getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public void setCompatabilityVersion(Version compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getCompatibilityVersion() == null) ? 0 : getCompatibilityVersion().hashCode());
        result = prime * result + ((getNetwork() == null) ? 0 : getNetwork().hashCode());
        result = prime * result + ((getStoragePoolName() == null) ? 0 : getStoragePoolName().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof NetworkView)) {
            return false;
        }
        NetworkView other = (NetworkView) obj;
        if (getCompatibilityVersion() == null) {
            if (other.getCompatibilityVersion() != null) {
                return false;
            }
        } else if (!getCompatibilityVersion().equals(other.getCompatibilityVersion())) {
            return false;
        }
        if (getNetwork() == null) {
            if (other.getNetwork() != null) {
                return false;
            }
        } else if (!getNetwork().equals(other.getNetwork())) {
            return false;
        }
        if (getStoragePoolName() == null) {
            if (other.getStoragePoolName() != null) {
                return false;
            }
        } else if (!getStoragePoolName().equals(other.getStoragePoolName())) {
            return false;
        }
        return true;
    }
}
