package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import javax.validation.Valid;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class AnonymousHostNetworkQos implements BusinessEntity<Guid> {

    private static final long serialVersionUID = 2692739166236838360L;

    private static final String UNLIMITED = "Unlimited";
    private Guid id = Guid.Empty;

    @Valid
    private HostNetworkQosProperties hostNetworkQosProperties = new HostNetworkQosProperties();

    public static AnonymousHostNetworkQos fromHostNetworkQos(HostNetworkQos hostNetworkQos) {
        if (hostNetworkQos == null) {
            return null;
        }

        AnonymousHostNetworkQos result = new AnonymousHostNetworkQos();
        result.id = hostNetworkQos.getId();
        result.hostNetworkQosProperties = hostNetworkQos.getHostNetworkQosProperties();

        return result;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
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

    protected String renderQosParameter(Object qosParameter) {
        return (qosParameter == null) ? UNLIMITED : String.valueOf(qosParameter);
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
        if (!(o instanceof AnonymousHostNetworkQos)) {
            return false;
        }
        AnonymousHostNetworkQos that = (AnonymousHostNetworkQos) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(hostNetworkQosProperties, that.hostNetworkQosProperties);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), hostNetworkQosProperties);
    }
}
