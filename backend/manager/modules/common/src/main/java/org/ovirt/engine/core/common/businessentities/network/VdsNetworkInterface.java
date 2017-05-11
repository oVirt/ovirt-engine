package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.ovirt.engine.core.common.network.SwitchType;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.Ipv4;
import org.ovirt.engine.core.common.validation.annotation.Ipv6;
import org.ovirt.engine.core.common.validation.annotation.Mask;
import org.ovirt.engine.core.common.validation.annotation.ValidNameOfVdsNetworkInterface;
import org.ovirt.engine.core.common.validation.annotation.ValidNetworkConfiguration;
import org.ovirt.engine.core.common.validation.annotation.ValidNetworkLabelFormat;
import org.ovirt.engine.core.common.vdscommands.UserOverriddenNicValues;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VdsNetworkInterface</code> defines a type of {@link NetworkInterface} for instances of
 * {@link org.ovirt.engine.core.common.businessentities.VDS}.
 *
 */
@ValidNetworkConfiguration
public class VdsNetworkInterface extends NetworkInterface<VdsNetworkStatistics> {
    private static final long serialVersionUID = -6347816237220936283L;

    private Guid vdsId;
    private String vdsName;
    private String networkName;

    /**
     * Switch type of network attached to this nic, as reported by vdsm.
     */
    private SwitchType reportedSwitchType;

    private Ipv4BootProtocol ipv4BootProtocol;

    @Ipv4(message = "IPV4_ADDR_BAD_FORMAT")
    private String ipv4Address;

    @Mask
    private String ipv4Subnet;

    private boolean ipv4DefaultRoute;

    @Ipv4(message = "IPV4_ADDR_GATEWAY_BAD_FORMAT")
    private String ipv4Gateway;

    private Ipv6BootProtocol ipv6BootProtocol;

    @Ipv6
    private String ipv6Address;

    @Min(0L)
    @Max(128L)
    private Integer ipv6Prefix;

    @Ipv6
    private String ipv6Gateway;

    private String baseInterface;
    private Integer vlanId;
    private Boolean bonded;
    private String bondName;
    private Integer bondType;
    private String bondOptions;
    private int mtu;
    private boolean bridged;
    private NetworkImplementationDetails networkImplementationDetails;
    private HostNetworkQos qos;
    private String adPartnerMac;
    private Integer adAggregatorId;


    @ValidNetworkLabelFormat(message = "NETWORK_LABEL_FORMAT_INVALID")
    private Set<String> labels;

    public VdsNetworkInterface() {
        super(new VdsNetworkStatistics(), VdsInterfaceType.NONE.getValue());
    }

    @Override
    @ValidNameOfVdsNetworkInterface
    public String getName() {
        return super.getName();
    }

    /**
     * Returns if this is the management interface.
     *
     * @return <code>true</code> if this is the management interface
     */
    public boolean getIsManagement() {
        return getType() != null && ((getType() & 2) > 0);
    }

    /**
     * Sets the related VDS id.
     *
     * @param vdsId
     *            the id
     */
    public void setVdsId(Guid vdsId) {
        this.vdsId = vdsId;
        this.statistics.setVdsId(vdsId);
    }

    /**
     * Returns the VDS id.
     *
     * @return the id
     */
    public Guid getVdsId() {
        return vdsId;
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
     * @param ipv4BootProtocol
     *            the boot protocol
     */
    public void setIpv4BootProtocol(Ipv4BootProtocol ipv4BootProtocol) {
        this.ipv4BootProtocol = ipv4BootProtocol;
    }

    /**
     * Returns the boot protocol.
     *
     * @return the boot protocol
     */
    public Ipv4BootProtocol getIpv4BootProtocol() {
        return ipv4BootProtocol;
    }

    public String getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    public Ipv6BootProtocol getIpv6BootProtocol() {
        return ipv6BootProtocol;
    }

    public void setIpv6BootProtocol(Ipv6BootProtocol ipv6BootProtocol) {
        this.ipv6BootProtocol = ipv6BootProtocol;
    }

    public String getIpv6Gateway() {
        return ipv6Gateway;
    }

    public void setIpv6Gateway(String ipv6Gateway) {
        this.ipv6Gateway = ipv6Gateway;
    }

    public Integer getIpv6Prefix() {
        return ipv6Prefix;
    }

    public void setIpv6Prefix(Integer ipv6Prefix) {
        this.ipv6Prefix = ipv6Prefix;
    }

    /**
     * Sets the name of the network.
     *
     * @param networkName
     *            the network name
     */
    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    /**
     * Returns the name of the network.
     *
     * @return the network name
     */
    public String getNetworkName() {
        return networkName;
    }

    /**
     * Sets the network ipv4Address.
     *
     * @param ipv4Address
     *            the ipv4Address
     */
    public void setIpv4Address(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    /**
     * Returns the network ipv4Address.
     *
     * @return the ipv4Address
     */
    public String getIpv4Address() {
        return ipv4Address;
    }

    /**
     * Sets the ipv4Address's ipv4Subnet.
     *
     * @param ipv4Subnet
     *            the ipv4Subnet
     */
    public void setIpv4Subnet(String ipv4Subnet) {
        this.ipv4Subnet = ipv4Subnet;
    }

    /**
     * Returns the ipv4Subnet.
     *
     * @return the ipv4Subnet
     */
    public String getIpv4Subnet() {
        return ipv4Subnet;
    }

    /**
     * Sets the ipv4Gateway.
     *
     * @param ipv4Gateway
     *            the ipv4Gateway
     */
    public void setIpv4Gateway(String ipv4Gateway) {
        this.ipv4Gateway = ipv4Gateway;
    }

    /**
     * Returns the ipv4Gateway.
     *
     * @return the ipv4Gateway
     */
    public String getIpv4Gateway() {
        return ipv4Gateway;
    }

    /**
     * If the interface is vlan set its base interface name
     *
     * @param baseInterface
     *            the base interface name
     */
    public void setBaseInterface(String baseInterface) {
        this.baseInterface = baseInterface;
    }

    /**
     * Returns the base interface name.
     *
     * @return baseInterface
     */
    public String getBaseInterface() {
        return baseInterface;
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
     * @return <code>true</code> if it is bonded, <code>null</code> if value is not set.
     */
    public Boolean getBonded() {
        return bonded;
    }

    public boolean isBond() {
        return Boolean.TRUE.equals(getBonded());
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
     * Checks whether an interface is part of a bond.
     *
     * @return whether the interface is part of a bond.
     */
    public boolean isPartOfBond() {
        return getBondName() != null;
    }

    public boolean isPartOfBond(String bondName) {
        return isPartOfBond() && getBondName().equals(bondName);
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

    public int getMtu() {
        return mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;

    }

    public boolean isBridged() {
        return bridged;
    }

    public void setBridged(boolean bridged) {
        this.bridged = bridged;
    }

    public SwitchType getReportedSwitchType() {
        return reportedSwitchType;
    }

    public void setReportedSwitchType(SwitchType reportedSwitchType) {
        this.reportedSwitchType = reportedSwitchType;
    }

    public NetworkImplementationDetails getNetworkImplementationDetails() {
        return networkImplementationDetails;
    }

    public void setNetworkImplementationDetails(NetworkImplementationDetails networkImplementationDetails) {
        this.networkImplementationDetails = networkImplementationDetails;
    }

    /**
     * Gets the QoS configured on this interface, which overrides the one possibly configured on the network.
     */
    public HostNetworkQos getQos() {
        return qos;
    }

    /**
     * Sets the QoS configured on this interface, which overrides the one possibly configured on the network. Note that
     * overriding QoS entities must not contain a name nor a DC ID!
     */
    public void setQos(HostNetworkQos qos) {
        if (qos != null) {
            assert qos.getName() == null : "Overriding QoS entity must not be named!";
            assert qos.getStoragePoolId() == null : "Overriding QoS entity must not be linked to a specific DC!";
        }

        this.qos = qos;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    /**
     * Preserves nic attributes which are managed by the engine
     *
     * @param sourceNic
     *            the given nic which its attributes should override the one's of the nic
     */
    public void overrideEngineManagedAttributes(VdsNetworkInterface sourceNic) {
        setLabels(sourceNic.getLabels());
    }

    public void overrideEngineManagedAttributes(UserOverriddenNicValues userOverriddenNicValues) {
        if (userOverriddenNicValues != null) {
            setLabels(userOverriddenNicValues.getLabels());
        }
    }

    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return tsb.append("id", getId())
                .append("name", getName())
                .append("vdsId", getVdsId())
                .append("networkName", getNetworkName())
                .append("ipv4BootProtocol", getIpv4BootProtocol())
                .append("ipv4Address", getIpv4Address())
                .append("ipv4Subnet", getIpv4Subnet())
                .append("ipv4Gateway", getIpv4Gateway())
                .append("ipv6BootProtocol", getIpv6BootProtocol())
                .append("ipv6Address", getIpv6Address())
                .append("ipv6Prefix", getIpv6Prefix())
                .append("ipv6Gateway", getIpv6Gateway())
                .append("mtu", getMtu())
                .append("bridged", isBridged())
                .append("reportedSwitchType", getReportedSwitchType())
                .append("type", getType())
                .append("networkImplementationDetails", getNetworkImplementationDetails())
                .append("qos", getQos())
                .append("adPartnerMac", getAdPartnerMac())
                .append("adAggregatorId", adAggregatorId);
    }

    @Override
    public String toString() {
        return appendAttributes(ToStringBuilder.forInstance(this)).build();
    }

    public String getAdPartnerMac() {
        return adPartnerMac;
    }

    public void setAdPartnerMac(String adPartnerMac) {
        this.adPartnerMac = adPartnerMac;
    }

    public Integer getAdAggregatorId() {
        return adAggregatorId;
    }

    public void setAdAggregatorId(Integer adAggregationId) {
        this.adAggregatorId = adAggregationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                ipv4Address,
                ipv6Address,
                bondName,
                bondOptions,
                bondType,
                bonded,
                ipv4BootProtocol,
                ipv6BootProtocol,
                reportedSwitchType,
                networkName,
                bridged,
                ipv4Gateway,
                ipv4DefaultRoute,
                ipv6Gateway,
                mtu,
                ipv4Subnet,
                ipv6Prefix,
                vdsId,
                baseInterface,
                vlanId,
                qos,
                labels,
                adPartnerMac,
                adAggregatorId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VdsNetworkInterface)) {
            return false;
        }
        VdsNetworkInterface other = (VdsNetworkInterface) obj;
        return super.equals(obj)
                && Objects.equals(ipv4Address, other.ipv4Address)
                && Objects.equals(ipv6Address, other.ipv6Address)
                && Objects.equals(bondName, other.bondName)
                && Objects.equals(bondOptions, other.bondOptions)
                && Objects.equals(bondType, other.bondType)
                && Objects.equals(bonded, other.bonded)
                && ipv4BootProtocol == other.ipv4BootProtocol
                && ipv6BootProtocol == other.ipv6BootProtocol
                && Objects.equals(reportedSwitchType, other.reportedSwitchType)
                && Objects.equals(networkName, other.networkName)
                && bridged == other.bridged
                && Objects.equals(ipv4Gateway, other.ipv4Gateway)
                && Objects.equals(ipv4DefaultRoute, other.ipv4DefaultRoute)
                && Objects.equals(ipv6Gateway, other.ipv6Gateway)
                && mtu == other.mtu
                && Objects.equals(ipv4Subnet, other.ipv4Subnet)
                && Objects.equals(ipv6Prefix, other.ipv6Prefix)
                && Objects.equals(vdsId, other.vdsId)
                && Objects.equals(baseInterface, other.baseInterface)
                && Objects.equals(vlanId, other.vlanId)
                && Objects.equals(qos, other.qos)
                && Objects.equals(labels, other.labels)
                && Objects.equals(adPartnerMac, other.adPartnerMac)
                && Objects.equals(adAggregatorId, other.adAggregatorId);
    }

    public boolean isIpv4DefaultRoute() {
        return ipv4DefaultRoute;
    }

    public void setIpv4DefaultRoute(boolean ipv4DefaultRoute) {
        this.ipv4DefaultRoute = ipv4DefaultRoute;
    }

    /**
     * Holds various details about regarding the logical network implementation on the device.
     */
    public static class NetworkImplementationDetails implements Serializable{

        private static final long serialVersionUID = 5213991878221362832L;
        private boolean inSync;
        private boolean managed;

        public NetworkImplementationDetails() {
        }

        public NetworkImplementationDetails(boolean inSync, boolean managed) {
            this.inSync = inSync;
            this.managed = managed;
        }

        /**
         * @return Is the network's physical definition on the device same as the logical definition.
         */
        public boolean isInSync() {
            return inSync;
        }

        /**
         * @return Is the network that is defined on this interface managed by the engine, or some custom network which
         *         exists solely on the host.
         */
        public boolean isManaged() {
            return managed;
        }

        @Override
        public String toString() {
            return ToStringBuilder.forInstance(this)
                    .append("inSync", isInSync())
                    .append("managed", isManaged())
                    .build();
        }
    }
}
