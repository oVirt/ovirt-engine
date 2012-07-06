package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

public class AttachNetworkToVdsParameters extends VdsActionParameters {
    private static final long serialVersionUID = 5446434263733512827L;

    private boolean checkConnectivity;
    private Network network;
    private VdsNetworkInterface iface;
    private String address;
    private String subnet;
    private String gateway;
    private String bondingOptions;
    private NetworkBootProtocol bootProtocol = NetworkBootProtocol.None;

    public AttachNetworkToVdsParameters() {
    }

    public AttachNetworkToVdsParameters(Guid vdsId, Network net, VdsNetworkInterface iface) {
        super(vdsId);
        setCheckConnectivity(false);
        setNetwork(net);
        setInterface(iface);
    }

    public boolean getCheckConnectivity() {
        return checkConnectivity;
    }

    public void setCheckConnectivity(boolean value) {
        checkConnectivity = value;
    }

    public Network getNetwork() {
        return network;
    }

    public void setNetwork(Network value) {
        network = value;
    }

    public VdsNetworkInterface getInterface() {
        return iface;
    }

    public void setInterface(VdsNetworkInterface value) {
        iface = value;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String value) {
        address = value;
    }

    public String getSubnet() {
        return subnet;
    }

    public void setSubnet(String value) {
        subnet = value;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String value) {
        gateway = value;
    }

    public String getBondingOptions() {
        return bondingOptions;
    }

    public void setBondingOptions(String value) {
        bondingOptions = value;
    }

    public NetworkBootProtocol getBootProtocol() {
        return bootProtocol;
    }

    public void setBootProtocol(NetworkBootProtocol value) {
        bootProtocol = value;
    }
}
