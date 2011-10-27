package org.ovirt.engine.core.common.businessentities;

import java.util.ArrayList;
import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.NGuid;

/**
 * <code>VdsNetworkInterface</code> defines a type of {@link BaseNetworkInterface} for instances of {@link VDS}.
 *
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsNetworkInterface")
public class VdsNetworkInterface extends NetworkInterface<VdsNetworkStatistics> {
    private static final long serialVersionUID = -6347816237220936283L;

    private static final ArrayList<String> _changeablePropertiesList =
            new ArrayList<String>(Arrays.asList(new String[] {
                    "Id", "Name", "MacAddress", "NetworkName", "Type", "Speed", "Statistics", "VdsId", "VdsName",
                    "BootProtocol", "Address", "Subnet", "Gateway", "VlanId", "Bonded", "BondName", "BondType",
                    "BondOptions"
            }));

    @XmlElement(name = "VdsId", nillable = true)
    private NGuid vdsId;

    @XmlElement(name = "VdsName")
    private String vdsName;

    @XmlElement(name = "BootProtocol")
    private NetworkBootProtocol bootProtocol;

    @XmlElement(name = "Address")
    private String address;

    @XmlElement(name = "Subnet")
    private String subnet;

    @XmlElement(name = "Gateway")
    private String gateway;

    @XmlElement(name = "VlanId", nillable = true)
    private Integer vlanId;

    @XmlElement(name = "Bonded", nillable = true)
    private Boolean bonded;

    @XmlElement(name = "BondName")
    private String bondName;

    @XmlElement(name = "BondType", nillable = true)
    private Integer bondType;

    @XmlElement(name = "BondOptions", nillable = true)
    private String bondOptions;

    public VdsNetworkInterface() {
        super(new VdsNetworkStatistics(), VdsInterfaceType.None.getValue());
    }

    public void setIsManagement(boolean isManagement) {
        // TODO this method is to avoid xml errors
    }

    /**
     * Returns if this is the management interface.
     *
     * @return <code>true</code> if this is the management interface
     */
    @XmlElement(name = "IsManagement")
    public boolean getIsManagement() {
        return getType() != null && ((getType() & 2) > 0);
    }

    /**
     * Sets the related VDS id.
     *
     * @param vdsId
     *            the id
     */
    public void setVdsId(NGuid vdsId) {
        this.vdsId = vdsId;
        this.statistics.setVdsId(vdsId != null ? vdsId.getValue() : null);
    }

    /**
     * Returns the VDS id.
     *
     * @return the id
     */
    public NGuid getVdsId() {
        return (NGuid) vdsId;
    }

    /**
     * Sets the VDS entity's name.
     *
     * @param vdsName
     *            the name
     */
    public void setVdsName(String vdsName) {
        this.vdsName = vdsName;
    }

    /**
     * Returns the VDS entity's name.
     *
     * @return the name
     */
    public String getVdsName() {
        return vdsName;
    }

    /**
     * Sets the boot protocol.
     *
     * @param bootProtocol
     *            the boot protocol
     */
    public void setBootProtocol(NetworkBootProtocol bootProtocol) {
        this.bootProtocol = bootProtocol;
    }

    /**
     * Returns the boot protocol.
     *
     * @return the boot protocol
     */
    public NetworkBootProtocol getBootProtocol() {
        return bootProtocol;
    }

    /**
     * Sets the network address.
     *
     * @param address
     *            the address
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Returns the network address.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Sets the address's subnet.
     *
     * @param subnet
     *            the subnet
     */
    public void setSubnet(String subnet) {
        this.subnet = subnet;
    }

    /**
     * Returns the subnet.
     *
     * @return the subnet
     */
    public String getSubnet() {
        return subnet;
    }

    /**
     * Sets the gateway.
     *
     * @param gateway
     *            the gateway
     */
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    /**
     * Returns the gateway.
     *
     * @return the gateway
     */
    public String getGateway() {
        return gateway;
    }

    /**
     * Sets the VLAN id
     *
     * @param vlanId
     *            the VLAN id
     */
    public void setVlanId(Integer vlanId) {
        this.vlanId = vlanId;
    }

    /**
     * Returns the VLAN id.
     *
     * @return
     */
    public Integer getVlanId() {
        return vlanId;
    }

    /**
     * Sets whether the interface is bonded or not.
     *
     * @param bonded
     *            <code>true</code> if it is bonded
     */
    public void setBonded(Boolean bonded) {
        this.bonded = bonded;
    }

    /**
     * Returns if the interface is bonded or not.
     *
     * @return <code>true</code> if it is bonded
     */
    public Boolean getBonded() {
        return bonded;
    }

    /**
     * Sets the bond name.
     *
     * @param bondName
     *            the bond name
     */
    public void setBondName(String bondName) {
        this.bondName = bondName;
    }

    /**
     * Returns the bond name.
     *
     * @return the bond name
     */
    public String getBondName() {
        return bondName;
    }

    /**
     * Sets the bond type.
     *
     * @param bondType
     *            the bond type
     */
    public void setBondType(Integer bondType) {
        this.bondType = bondType;
    }

    /**
     * Returns the bond type.
     *
     * @return the bond type
     */
    public Integer getBondType() {
        return bondType;
    }

    /**
     * Sets the bond options.
     *
     * @param bondOptions
     *            the bond options
     */
    public void setBondOptions(String bondOptions) {
        this.bondOptions = bondOptions;
    }

    /**
     * Returns the bond options.
     *
     * @return the bond options
     */
    public String getBondOptions() {
        return bondOptions;
    }

    @Override
    public Object getQueryableId() {
        return id;
    }

    @Override
    public ArrayList<String> getChangeablePropertiesList() {
        return _changeablePropertiesList;
    }
}
