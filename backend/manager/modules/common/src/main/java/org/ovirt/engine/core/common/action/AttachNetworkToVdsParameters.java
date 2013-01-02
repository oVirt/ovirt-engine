package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;

import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;

public class AttachNetworkToVdsParameters extends VdsActionParameters {
    private static final long serialVersionUID = 5446434263733512827L;

    private boolean checkConnectivity;

    @Valid
    private Network network;
    private String oldNetworkName;
    private VdsNetworkInterface iface;

    @Pattern(regexp = ValidationUtils.IP_PATTERN, message = "NETWORK_ADDR_IN_STATIC_IP_BAD_FORMAT")
    private String address;

    @Pattern(regexp = ValidationUtils.IP_PATTERN, message = "NETWORK_ADDR_IN_SUBNET_BAD_FORMAT")
    private String subnet;

    @Pattern(regexp = ValidationUtils.IP_PATTERN, message = "NETWORK_ADDR_IN_GATEWAY_BAD_FORMAT")
    private String gateway;
    private String bondingOptions;
    private NetworkBootProtocol bootProtocol = NetworkBootProtocol.NONE;

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

    public String getOldNetworkName() {
        return oldNetworkName;
    }

    public void setOldNetworkName(String value) {
        oldNetworkName = value;
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
