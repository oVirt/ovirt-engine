package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class NetworkParameters {

    private NetworkBootProtocol bootProtocol;
    private String address;
    private String subnet;
    private String gateway;
    private boolean qosOverridden;
    private HostNetworkQos qos;
    private Map<String, String> customProperties;


    public NetworkParameters() {
    }

    public NetworkParameters(VdsNetworkInterface nic) {
        setBootProtocol(nic.getBootProtocol());
        setAddress(nic.getAddress());
        setSubnet(nic.getSubnet());
        setGateway(nic.getGateway());
        setQosOverridden(nic.isQosOverridden());
        setQos(nic.getQos());
        setCustomProperties(nic.getCustomProperties());
    }

    public NetworkBootProtocol getBootProtocol() {
        return bootProtocol;
    }
    public void setBootProtocol(NetworkBootProtocol bootProtocol) {
        this.bootProtocol = bootProtocol;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public String getSubnet() {
        return subnet;
    }
    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }
    public String getGateway() {
        return gateway;
    }
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public boolean getQosOverridden() {
        return qosOverridden;
    }

    public void setQosOverridden(boolean qosOverridden) {
        this.qosOverridden = qosOverridden;
    }

    public HostNetworkQos getQos() {
        return qos;
    }

    public void setQos(HostNetworkQos qos) {
        this.qos = qos;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

}
