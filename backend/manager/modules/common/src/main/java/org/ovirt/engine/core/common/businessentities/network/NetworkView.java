package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import org.ovirt.engine.core.compat.Version;

public class NetworkView extends Network {

    private static final long serialVersionUID = 7541192304006710467L;

    private String dataCenterName;
    private Version compatibilityVersion;
    private String providerName;
    private String qosName;

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

    public String getQosName() {
        return qosName;
    }

    public void setQosName(String qosName) {
        this.qosName = qosName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                compatibilityVersion,
                dataCenterName,
                providerName,
                qosName
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof NetworkView)) {
            return false;
        }
        NetworkView other = (NetworkView) obj;
        return super.equals(obj)
                && Objects.equals(compatibilityVersion, other.compatibilityVersion)
                && Objects.equals(dataCenterName, other.dataCenterName)
                && Objects.equals(providerName, other.providerName)
                && Objects.equals(qosName, other.qosName);
    }
}
