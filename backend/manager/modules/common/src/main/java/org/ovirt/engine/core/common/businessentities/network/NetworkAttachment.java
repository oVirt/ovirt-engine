package org.ovirt.engine.core.common.businessentities.network;

import java.util.Map;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.NetworkIdOrNetworkNameIsSet;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

@NetworkIdOrNetworkNameIsSet(groups = { CreateEntity.class, UpdateEntity.class })
public class NetworkAttachment implements Queryable, BusinessEntity<Guid> {

    private static final long serialVersionUID = -8052325342869681284L;

    @NotNull(groups = UpdateEntity.class, message = "NETWORK_ATTACHMENTS_ID_MUST_BE_SET_FOR_UPDATE")
    private Guid id;

    private Guid networkId;

    private String networkName;

    private Guid nicId;

    @Valid
    private AnonymousHostNetworkQos hostNetworkQos;

    private String nicName;

    @Valid
    private IpConfiguration ipConfiguration;

    private Map<String, String> properties;
    private boolean overrideConfiguration;
    private ReportedConfigurations reportedConfigurations;

    @Valid
    private DnsResolverConfiguration dnsResolverConfiguration;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public NetworkAttachment() {
    }

    public NetworkAttachment(NetworkAttachment networkAttachment) {
        id = networkAttachment.getId();
        nicId = networkAttachment.getNicId();
        nicName = networkAttachment.getNicName();
        networkId = networkAttachment.getNetworkId();
        networkName = networkAttachment.getNetworkName();
        ipConfiguration = networkAttachment.getIpConfiguration();
        properties = networkAttachment.getProperties();
        overrideConfiguration = networkAttachment.isOverrideConfiguration();
        reportedConfigurations = networkAttachment.getReportedConfigurations();
    }

    public NetworkAttachment(VdsNetworkInterface baseNic, Network network, IpConfiguration ipConfiguration) {
        this.networkId = network.getId();
        this.networkName = network.getName();

        this.nicId = baseNic.getId();
        this.nicName = baseNic.getName();

        this.ipConfiguration = ipConfiguration;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;

        if (isQosOverridden()) {
            hostNetworkQos.setId(id);
        }
    }

    public Guid getNetworkId() {
        return networkId;
    }

    public void setNetworkId(Guid networkId) {
        this.networkId = networkId;
    }

    public Guid getNicId() {
        return nicId;
    }

    public void setNicId(Guid nicId) {
        this.nicId = nicId;
    }

    public String getNicName() {
        return nicName;
    }

    public void setNicName(String nicName) {
        this.nicName = nicName;
    }

    public IpConfiguration getIpConfiguration() {
        return ipConfiguration;
    }

    public void setIpConfiguration(IpConfiguration ipConfiguration) {
        this.ipConfiguration = ipConfiguration;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public boolean hasProperties() {
        return !(getProperties() == null || getProperties().isEmpty());
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public boolean isOverrideConfiguration() {
        return overrideConfiguration;
    }

    public void setOverrideConfiguration(boolean overrideConfiguration) {
        this.overrideConfiguration = overrideConfiguration;
    }

    public void setReportedConfigurations(ReportedConfigurations reportedConfigurations) {
        this.reportedConfigurations = reportedConfigurations;
    }

    public ReportedConfigurations getReportedConfigurations() {
        return reportedConfigurations;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public AnonymousHostNetworkQos getHostNetworkQos() {
        return hostNetworkQos;
    }

    public void setHostNetworkQos(AnonymousHostNetworkQos hostNetworkQos) {
        this.hostNetworkQos = hostNetworkQos;
        if (this.hostNetworkQos != null) {
            this.hostNetworkQos.setId(this.getId());
        }
    }

    public boolean isQosOverridden() {
        return hostNetworkQos != null;
    }

    public DnsResolverConfiguration getDnsResolverConfiguration() {
        return dnsResolverConfiguration;
    }

    public void setDnsResolverConfiguration(DnsResolverConfiguration dnsResolverConfiguration) {
        this.dnsResolverConfiguration = dnsResolverConfiguration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NetworkAttachment)) {
            return false;
        }
        NetworkAttachment other = (NetworkAttachment) o;
        return Objects.equals(networkId, other.networkId)
                && Objects.equals(nicId, other.nicId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                networkId,
                nicId
        );
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("networkId", getNetworkId())
                .append("networkName", getNetworkName())
                .append("nicId", getNicId())
                .append("nicName", getNicName())
                .append("ipConfiguration", getIpConfiguration())
                .append("dnsResolverConfiguration", getDnsResolverConfiguration())
                .append("properties", getProperties())
                .append("overrideConfiguration", isOverrideConfiguration())
                .build();
    }
}
