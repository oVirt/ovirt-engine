package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddBondParameters")
public class AddBondParameters extends RemoveBondParameters {
    private static final long serialVersionUID = 761203751697100144L;
    @XmlElement(name = "Nics")
    private String[] privateNics;

    public String[] getNics() {
        return privateNics == null ? new String[0] : privateNics;
    }

    public void setNics(String[] value) {
        privateNics = value;
    }

    @XmlElement(name = "VlanTag")
    private Integer privateVlanTag;

    public Integer getVlanTag() {
        return privateVlanTag;
    }

    public void setVlanTag(Integer value) {
        privateVlanTag = value;
    }

    @XmlElement(name = "Network")
    private network privateNetwork;

    public network getNetwork() {
        return privateNetwork;
    }

    public void setNetwork(network value) {
        privateNetwork = value;
    }

    @XmlElement(name = "Address")
    private String privateAddress;

    public String getAddress() {
        return privateAddress;
    }

    public void setAddress(String value) {
        privateAddress = value;
    }

    @XmlElement(name = "Subnet")
    private String privateSubnet;

    public String getSubnet() {
        return privateSubnet;
    }

    public void setSubnet(String value) {
        privateSubnet = value;
    }

    @XmlElement(name = "Gateway")
    private String privateGateway;

    public String getGateway() {
        return privateGateway;
    }

    public void setGateway(String value) {
        privateGateway = value;
    }

    @XmlElement(name = "VlanId")
    private Integer privateVlanId;

    public Integer getVlanId() {
        return privateVlanId;
    }

    public void setVlanId(Integer value) {
        privateVlanId = value;
    }

    @XmlElement(name = "BondingOptions")
    private String privateBondingOptions;

    public String getBondingOptions() {
        return privateBondingOptions;
    }

    public void setBondingOptions(String value) {
        privateBondingOptions = value;
    }

    @XmlElement(name = "BootProtocol")
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
