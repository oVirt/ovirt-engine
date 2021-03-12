package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;

public class VnicProfileView extends VnicProfile {

    private static final long serialVersionUID = -7873671947250939737L;
    private String networkName;
    private String networkQosName;
    private String dataCenterName;
    private Version compatibilityVersion;
    private String networkFilterName;
    private Guid dataCenterId;
    private String failoverVnicProfileName;

    public static final VnicProfileView EMPTY = new VnicProfileView();

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getNetworkQosName() {
        return networkQosName;
    }

    public void setNetworkQosName(String networkQosName) {
        this.networkQosName = networkQosName;
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

    public String getNetworkFilterName() {
        return networkFilterName;
    }

    public void setNetworkFilterName(String networkFilterName) {
        this.networkFilterName = networkFilterName;
    }

    public Guid getDataCenterId() {
        return this.dataCenterId;
    }

    public void setDataCenterId(Guid id) {
        this.dataCenterId = id;
    }

    public String getFailoverVnicProfileName() {
        return failoverVnicProfileName;
    }

    public void setFailoverVnicProfileName(String failoverVnicProfileName) {
        this.failoverVnicProfileName = failoverVnicProfileName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                compatibilityVersion,
                dataCenterName,
                networkName,
                networkQosName,
                networkFilterName,
                dataCenterId,
                failoverVnicProfileName
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VnicProfileView)) {
            return false;
        }
        VnicProfileView other = (VnicProfileView) obj;
        return super.equals(obj)
                && Objects.equals(compatibilityVersion, other.compatibilityVersion)
                && Objects.equals(dataCenterName, other.dataCenterName)
                && Objects.equals(networkName, other.networkName)
                && Objects.equals(networkQosName, other.networkQosName)
                && Objects.equals(networkFilterName, other.networkFilterName)
                && Objects.equals(dataCenterId, other.dataCenterId)
                && Objects.equals(failoverVnicProfileName, other.failoverVnicProfileName);
    }
}
