package org.ovirt.engine.core.common.vdscommands;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NameServer;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class HostNetwork {
    private Network network;
    private NetworkAttachment networkAttachment;
    private boolean defaultRoute;
    private boolean bonding;
    private boolean qosConfiguredOnInterface;
    private HostNetworkQos qos;
    private List<NameServer> nameServers;

    @SuppressWarnings("unused")
    private HostNetwork() {
    }

    public HostNetwork(Network network, NetworkAttachment networkAttachment) {
        this.network = network;
        this.networkAttachment = networkAttachment;
    }

    public String getNetworkName() {
        return network.getName();
    }

    public String getVdsmName() {
        return network.getVdsmName();
    }

    public String getNicName() {
        return networkAttachment.getNicName();
    }

    public boolean isDefaultRoute() {
        return defaultRoute;
    }

    public void setDefaultRoute(boolean defaultRoute) {
        this.defaultRoute = defaultRoute;
    }

    public boolean isBonding() {
        return bonding;
    }

    public void setBonding(boolean bonding) {
        this.bonding = bonding;
    }

    public Integer getVlan() {
        return network.getVlanId();
    }

    public boolean isVlan() {
        return network.getVlanId() != null;
    }

    public Network getNetwork() {
        return network;
    }

    public boolean isVmNetwork() {
        return network.isVmNetwork();
    }

    public boolean isStp() {
        return network.getStp();
    }

    public Map<String, String> getProperties() {
        return networkAttachment.getProperties();
    }

    public boolean hasProperties() {
        return getProperties() != null && !getProperties().isEmpty();
    }

    public Ipv4BootProtocol getIpv4BootProtocol() {
        if (primaryIpv4AddressNotSet()) {
            return null;
        }

        return getIpConfiguration().getIpv4PrimaryAddress().getBootProtocol();
    }

    public String getIpv4Address() {
        if (primaryIpv4AddressNotSet()) {
            return null;
        }

        return getIpConfiguration().getIpv4PrimaryAddress().getAddress();
    }

    public String getIpv4Netmask() {
        if (primaryIpv4AddressNotSet()) {
            return null;
        }

        return getIpConfiguration().getIpv4PrimaryAddress().getNetmask();
    }

    public String getIpv4Gateway() {
        if (primaryIpv4AddressNotSet()) {
            return null;
        }

        return getIpConfiguration().getIpv4PrimaryAddress().getGateway();
    }

    public Ipv6BootProtocol getIpv6BootProtocol() {
        if (primaryIpv6AddressNotSet()) {
            return null;
        }

        return getIpConfiguration().getIpv6PrimaryAddress().getBootProtocol();
    }

    public String getIpv6Address() {
        if (primaryIpv6AddressNotSet()) {
            return null;
        }

        return getIpConfiguration().getIpv6PrimaryAddress().getAddress();
    }

    public Integer getIpv6Prefix() {
        if (primaryIpv6AddressNotSet()) {
            return null;
        }

        return getIpConfiguration().getIpv6PrimaryAddress().getPrefix();
    }

    public String getIpv6Gateway() {
        if (primaryIpv6AddressNotSet()) {
            return null;
        }

        return getIpConfiguration().getIpv6PrimaryAddress().getGateway();
    }

    private boolean primaryIpv4AddressNotSet() {
        return !hasIpConfiguration() || !getIpConfiguration().hasIpv4PrimaryAddressSet();
    }

    private boolean primaryIpv6AddressNotSet() {
        return !hasIpConfiguration() || !getIpConfiguration().hasIpv6PrimaryAddressSet();
    }

    private boolean hasIpConfiguration() {
        return getIpConfiguration() != null;
    }

    private IpConfiguration getIpConfiguration() {
        return networkAttachment.getIpConfiguration();
    }

    public void setQosConfiguredOnInterface(boolean qosConfiguredOnInterface) {
        this.qosConfiguredOnInterface = qosConfiguredOnInterface;
    }

    public boolean isQosConfiguredOnInterface() {
        return qosConfiguredOnInterface;
    }

    public void setQos(HostNetworkQos qos) {
        this.qos = qos;
    }

    public HostNetworkQos getQos() {
        return qos;
    }

    public void setNameServers(List<NameServer> nameServers) {
        this.nameServers = nameServers;
    }

    public List<NameServer> getNameServers() {
        return nameServers;
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("defaultRoute", isDefaultRoute())
                .append("bonding", isBonding())
                .append("networkName", getNetworkName())
                .append("vdsmName", getVdsmName())
                .append("nicName", getNicName())
                .append("vlan", getVlan())
                .append("vmNetwork", isVmNetwork())
                .append("stp", isStp())
                .append("properties", getProperties())
                .append("ipv4BootProtocol", getIpv4BootProtocol())
                .append("ipv4Address", getIpv4Address())
                .append("ipv4Netmask", getIpv4Netmask())
                .append("ipv4Gateway", getIpv4Gateway())
                .append("ipv6BootProtocol", getIpv6BootProtocol())
                .append("ipv6Address", getIpv6Address())
                .append("ipv6Prefix", getIpv6Prefix())
                .append("ipv6Gateway", getIpv6Gateway())
                .append("nameServers", getNameServers())
                .build();
    }
}
