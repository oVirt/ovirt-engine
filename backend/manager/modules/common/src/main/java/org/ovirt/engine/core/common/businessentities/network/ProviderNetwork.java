package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class ProviderNetwork implements Serializable {

    private static final long serialVersionUID = -1080092693244154625L;
    private static final String FLAT_NETWORK = "flat";
    private static final String VLAN_NETWORK = "vlan";

    private Guid providerId;

    private String externalId;

    private Guid physicalNetworkId;

    private String customPhysicalNetworkName;

    @Min(value = 0, message = "NETWORK_VLAN_OUT_OF_RANGE", groups = { CreateEntity.class, UpdateEntity.class })
    @Max(value = 4094, message = "NETWORK_VLAN_OUT_OF_RANGE", groups = { CreateEntity.class, UpdateEntity.class })
    private Integer externalVlanId;

    private String providerNetworkType;

    private Boolean portSecurityEnabled;

    public ProviderNetwork() {
    }

    public ProviderNetwork(Guid providerId, String externalId) {
        this.providerId = providerId;
        this.externalId = externalId;
    }

    public ProviderNetwork(Guid providerId, String externalId, Guid physicalNetworkId) {
        this(providerId, externalId);
        this.physicalNetworkId = physicalNetworkId;
    }

    public Guid getProviderId() {
        return providerId;
    }

    public void setProviderId(Guid providerId) {
        this.providerId = providerId;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Guid getPhysicalNetworkId() {
        return physicalNetworkId;
    }

    public void setPhysicalNetworkId(Guid physicalNetworkId) {
        this.physicalNetworkId = physicalNetworkId;
    }

    public boolean isSetPhysicalNetworkId() {
        return physicalNetworkId != null;
    }

    public String getCustomPhysicalNetworkName() {
        return customPhysicalNetworkName;
    }

    public void setCustomPhysicalNetworkName(String customPhysicalNetworkName) {
        this.customPhysicalNetworkName = customPhysicalNetworkName;
    }

    public boolean hasCustomPhysicalNetworkName() {
        return getCustomPhysicalNetworkName() != null;
    }

    public boolean isLinkedToPhysicalNetwork() {
        return isSetPhysicalNetworkId() || hasCustomPhysicalNetworkName();
    }

    public Integer getExternalVlanId() {
        return externalVlanId;
    }

    public void setExternalVlanId(Integer externalVlanId) {
        this.externalVlanId = externalVlanId;
    }

    public boolean hasExternalVlanId() {
        return getExternalVlanId() != null;
    }

    public void setProviderNetworkType(String providerNetworkType) {
        this.providerNetworkType = providerNetworkType;
    }

    public String getProviderNetworkType() {
        return providerNetworkType;
    }

    public boolean isProviderNetworkFlat() {
        return FLAT_NETWORK.equals(getProviderNetworkType());
    }

    public boolean isProviderNetworkVlan() {
        return VLAN_NETWORK.equals(getProviderNetworkType());
    }

    public Boolean getPortSecurityEnabled() {
        return portSecurityEnabled;
    }

    public void setPortSecurityEnabled(Boolean portSecurityEnabled) {
        this.portSecurityEnabled = portSecurityEnabled;
    }

    public boolean isPortSecurityConfigured() {
        return this.portSecurityEnabled != null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                externalId,
                providerId,
                physicalNetworkId,
                customPhysicalNetworkName,
                externalVlanId,
                providerNetworkType
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ProviderNetwork)) {
            return false;
        }
        ProviderNetwork other = (ProviderNetwork) obj;
        return Objects.equals(externalId, other.externalId)
                && Objects.equals(providerId, other.providerId)
                && Objects.equals(physicalNetworkId, other.physicalNetworkId)
                && Objects.equals(customPhysicalNetworkName, other.customPhysicalNetworkName)
                && Objects.equals(externalVlanId, other.externalVlanId)
                && Objects.equals(providerNetworkType, other.providerNetworkType);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("providerId", getProviderId())
                .append("externalId", getExternalId())
                .append("physicalNetworkId", getPhysicalNetworkId())
                .append("customPhysicalNetworkName", getCustomPhysicalNetworkName())
                .append("externalVlanId", getExternalVlanId())
                .append("providerNetworkType", getProviderNetworkType())
                .build();
    }
}
