package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;

public class NetworkParameters {

    private NetworkBootProtocol bootProtocol;
    private String address;
    private String subnet;
    private String gateway;


    public NetworkParameters() {
    }

    public NetworkParameters(VdsNetworkInterface nic) {
        setBootProtocol(nic.getBootProtocol());
        setAddress(nic.getAddress());
        setSubnet(nic.getSubnet());
        setGateway(nic.getGateway());
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
}
