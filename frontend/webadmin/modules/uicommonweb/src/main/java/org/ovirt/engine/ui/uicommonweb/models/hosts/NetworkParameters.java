package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;

public class NetworkParameters implements InterfacePropertiesAccessor {

    private Ipv4BootProtocol bootProtocol;
    private String address;
    private String netmask;
    private String gateway;
    private boolean qosOverridden;
    private HostNetworkQos qos;
    private Map<String, String> customProperties;

    public NetworkParameters() {
    }

    @Override
    public Ipv4BootProtocol getBootProtocol() {
        return bootProtocol;
    }

    public void setBootProtocol(Ipv4BootProtocol bootProtocol) {
        this.bootProtocol = bootProtocol;
    }

    @Override
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    @Override
    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public boolean isQosOverridden() {
        return qosOverridden;
    }

    public void setQosOverridden(boolean qosOverridden) {
        this.qosOverridden = qosOverridden;
    }

    @Override
    public HostNetworkQos getHostNetworkQos() {
        return qos;
    }

    public void setHostNetworkQos(HostNetworkQos qos) {
        this.qos = qos;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

}
