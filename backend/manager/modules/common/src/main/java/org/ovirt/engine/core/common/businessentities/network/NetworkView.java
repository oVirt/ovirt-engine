package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import org.ovirt.engine.core.compat.Version;

public class NetworkView extends Network {

    private static final long serialVersionUID = 7541192304006710467L;

    private String dataCenterName;
    private Version compatibilityVersion;
    private String providerName;

    public String getDataCenterName() {
        return dataCenterName;
    }

    public void setStoragePoolName(String storagePoolName) {
        this.dataCenterName = storagePoolName;
    }

    public Version getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public void setCompatibilityVersion(Version compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getCompatibilityVersion() == null) ? 0 : getCompatibilityVersion().hashCode());
        result = prime * result + ((getDataCenterName() == null) ? 0 : getDataCenterName().hashCode());
        result = prime * result + ((getProviderName() == null) ? 0 : getProviderName().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
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
        if (getDataCenterName() == null) {
            if (other.getDataCenterName() != null) {
                return false;
            }
        } else if (!getDataCenterName().equals(other.getDataCenterName())) {
            return false;
        }
        if (Objects.equals(getProviderName(), other.getProviderName())) {
            return false;
        }
        return true;
    }
}
