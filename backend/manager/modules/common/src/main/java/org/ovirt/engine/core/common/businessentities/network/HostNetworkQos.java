package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.qos.QosBase;
import org.ovirt.engine.core.common.businessentities.qos.QosType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

/**
 * <a href="http://www.ovirt.org/develop/release-management/features/network/detailed-host-network-qos/">wiki doc</a>
 */
public class HostNetworkQos extends QosBase {

    private static final long serialVersionUID = 490527123959847064L;

    @Valid
    private HostNetworkQosProperties hostNetworkQosProperties = new HostNetworkQosProperties();

    public HostNetworkQos() {
        super(QosType.HOSTNETWORK);
    }

    public static HostNetworkQos fromAnonymousHostNetworkQos(AnonymousHostNetworkQos hostNetworkQos) {
        if (hostNetworkQos == null) {
            return null;
        }

        HostNetworkQos result = new HostNetworkQos();
        result.setId(hostNetworkQos.getId());
        result.hostNetworkQosProperties = new HostNetworkQosProperties(hostNetworkQos.getHostNetworkQosProperties());
        return result;
    }

    public Integer getOutAverageLinkshare() {
        return hostNetworkQosProperties.getOutAverageLinkshare();
    }

    public void setOutAverageLinkshare(Integer outAverageLinkshare) {
        hostNetworkQosProperties.setOutAverageLinkshare(outAverageLinkshare);
    }

    public Integer getOutAverageUpperlimit() {
        return hostNetworkQosProperties.getOutAverageUpperlimit();
    }

    public void setOutAverageUpperlimit(Integer outAverageUpperlimit) {
        hostNetworkQosProperties.setOutAverageUpperlimit(outAverageUpperlimit);
    }

    public Integer getOutAverageRealtime() {
        return hostNetworkQosProperties.getOutAverageRealtime();
    }

    public void setOutAverageRealtime(Integer outAverageRealtime) {
        hostNetworkQosProperties.setOutAverageRealtime(outAverageRealtime);
    }

    public boolean isEmpty() {
        return hostNetworkQosProperties.isEmpty();
    }

    HostNetworkQosProperties getHostNetworkQosProperties() {
        return hostNetworkQosProperties;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("outAverageLinkshare", renderQosParameter(getOutAverageLinkshare()))
                .append("outAverageUpperlimit", renderQosParameter(getOutAverageUpperlimit()))
                .append("outAverageRealtime", renderQosParameter(getOutAverageRealtime()))
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HostNetworkQos)) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        HostNetworkQos that = (HostNetworkQos) o;
        return Objects.equals(hostNetworkQosProperties, that.hostNetworkQosProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), hostNetworkQosProperties);
    }
}
