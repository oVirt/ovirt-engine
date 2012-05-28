package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;

public class AddBondParameters extends BondParametersBase {

    private static final long serialVersionUID = 761203751697100144L;

    private String[] nics;
    private network network;
    private String address;
    private String subnet;
    private Integer vlanId;
    private String gateway;
    private String bondingOptions;
    private NetworkBootProtocol privateBootProtocol = NetworkBootProtocol.None;

    public AddBondParameters() {
    }

    public AddBondParameters(Guid vdsId, String bondName, network network, String[] nics) {
        super(vdsId, bondName);
        setNics(nics);
        setNetwork(network);
    }

    public AddBondParameters(Guid vdsId, String bondName, network network, String[] nics, int vladId) {
        this(vdsId, bondName, network, nics);
        setVlanId(vladId);
    }

    public String[] getNics() {
        return nics == null ? new String[0] : nics;
    }

    public void setNics(String[] value) {
        nics = value;
    }

    public network getNetwork() {
        return network;
    }

    public void setNetwork(network value) {
        network = value;
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

    public Integer getVlanId() {
        return vlanId;
    }

    public void setVlanId(Integer value) {
        vlanId = value;
    }

    public String getBondingOptions() {
        return bondingOptions;
    }

    public void setBondingOptions(String value) {
        bondingOptions = value;
    }

    public NetworkBootProtocol getBootProtocol() {
        return privateBootProtocol;
    }

    public void setBootProtocol(NetworkBootProtocol value) {
        privateBootProtocol = value;
    }

}
