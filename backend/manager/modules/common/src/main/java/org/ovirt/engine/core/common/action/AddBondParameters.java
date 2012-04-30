package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class AddBondParameters extends RemoveBondParameters {
    private static final long serialVersionUID = 761203751697100144L;
    private String[] privateNics;

    public String[] getNics() {
        return privateNics == null ? new String[0] : privateNics;
    }

    public void setNics(String[] value) {
        privateNics = value;
    }

    private Integer privateVlanTag;

    public Integer getVlanTag() {
        return privateVlanTag;
    }

    public void setVlanTag(Integer value) {
        privateVlanTag = value;
    }

    private network privateNetwork;

    public network getNetwork() {
        return privateNetwork;
    }

    public void setNetwork(network value) {
        privateNetwork = value;
    }

    private String privateAddress;

    public String getAddress() {
        return privateAddress;
    }

    public void setAddress(String value) {
        privateAddress = value;
    }

    private String privateSubnet;

    public String getSubnet() {
        return privateSubnet;
    }

    public void setSubnet(String value) {
        privateSubnet = value;
    }

    private String privateGateway;

    public String getGateway() {
        return privateGateway;
    }

    public void setGateway(String value) {
        privateGateway = value;
    }

    private Integer privateVlanId;

    public Integer getVlanId() {
        return privateVlanId;
    }

    public void setVlanId(Integer value) {
        privateVlanId = value;
    }

    private String privateBondingOptions;

    public String getBondingOptions() {
        return privateBondingOptions;
    }

    public void setBondingOptions(String value) {
        privateBondingOptions = value;
    }

    private NetworkBootProtocol privateBootProtocol = NetworkBootProtocol.forValue(0);

    public NetworkBootProtocol getBootProtocol() {
        return privateBootProtocol;
    }

    public void setBootProtocol(NetworkBootProtocol value) {
        privateBootProtocol = value;
    }

    public AddBondParameters(Guid vdsId, String bondName, network network, String[] nics) {
        super(vdsId, bondName);
        setNics(nics);
        setNetwork(network);
    }

    public AddBondParameters(Guid vdsId, String bondName, network network, String[] nics, int vladId) {
        super(vdsId, bondName);
        setNics(nics);
        setNetwork(network);
        setVlanId(vladId);
    }

    public AddBondParameters() {
    }
}
