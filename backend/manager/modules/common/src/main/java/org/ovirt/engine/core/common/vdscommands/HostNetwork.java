package org.ovirt.engine.core.common.vdscommands;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class HostNetwork {
    private Network network;
    private NetworkAttachment networkAttachment;
    private boolean defaultRoute;
    private boolean bonding;
    private boolean qosConfiguredOnInterface;
    private HostNetworkQos qos;

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

    public int getMtu() {
        return network.getMtu();
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

    public NetworkBootProtocol getBootProtocol() {
        return hasIpConfiguration() ? getIpConfiguration().getBootProtocol() : null;
    }

    public String getAddress() {
        return hasIpConfiguration() ? getIpConfiguration().getAddress() : null;
    }

    public String getNetmask() {
        return hasIpConfiguration() ? getIpConfiguration().getNetmask() : null;
    }

    public String getGateway() {
        return hasIpConfiguration() ? getIpConfiguration().getGateway() : null;
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

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("defaultRoute", isDefaultRoute())
                .append("bonding", isBonding())
                .append("networkName", getNetworkName())
                .append("nicName", getNicName())
                .append("vlan", getVlan())
                .append("mtu", getMtu())
                .append("vmNetwork", isVmNetwork())
                .append("stp", isStp())
                .append("properties", getProperties())
                .append("bootProtocol", getBootProtocol())
                .append("address", getAddress())
                .append("netmask", getNetmask())
                .append("gateway", getGateway())
                .build();
    }
}
