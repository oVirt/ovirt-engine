package org.ovirt.engine.core.common.businessentities.network;

import org.ovirt.engine.core.compat.Version;


public class VnicProfileView extends VnicProfile {

    private static final long serialVersionUID = -7873671947250939737L;
    private String networkName;
    private String dataCenterName;
    private Version compatibilityVersion;

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getDataCenterName() {
        return dataCenterName;
    }

    public void setDataCenterName(String dataCenterName) {
        this.dataCenterName = dataCenterName;
    }

    public Version getCompatibilityVersion() {
        return compatibilityVersion;
    }

    public void setCompatibilityVersion(Version compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((getCompatibilityVersion() == null) ? 0 : getCompatibilityVersion().hashCode());
        result = prime * result + ((getDataCenterName() == null) ? 0 : getDataCenterName().hashCode());
        result = prime * result + ((getNetworkName() == null) ? 0 : getNetworkName().hashCode());
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
        if (!(obj instanceof VnicProfileView)) {
            return false;
        }
        VnicProfileView other = (VnicProfileView) obj;
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
        if (getNetworkName() == null) {
            if (other.getNetworkName() != null) {
                return false;
            }
        } else if (!getNetworkName().equals(other.getNetworkName())) {
            return false;
        }
        return true;
    }
}

