package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.ValidNetworkConfiguration;
import org.ovirt.engine.core.common.validation.annotation.ValidNetworkLabelFormat;
import org.ovirt.engine.core.compat.Guid;

/**
 * <code>VdsNetworkInterface</code> defines a type of {@link BaseNetworkInterface} for instances of {@link VDS}.
 *
 */
@ValidNetworkConfiguration
public class VdsNetworkInterface extends NetworkInterface<VdsNetworkStatistics> {
    private static final long serialVersionUID = -6347816237220936283L;

    private Guid vdsId;
    private String vdsName;
    private NetworkBootProtocol bootProtocol;
    private String networkName;

    @Pattern(regexp = ValidationUtils.IP_PATTERN, message = "NETWORK_ADDR_IN_STATIC_IP_BAD_FORMAT")
    private String address;

    @Pattern(regexp = ValidationUtils.IP_PATTERN, message = "NETWORK_ADDR_IN_SUBNET_BAD_FORMAT")
    private String subnet;

    @Pattern(regexp = ValidationUtils.IP_PATTERN, message = "NETWORK_ADDR_IN_GATEWAY_BAD_FORMAT")
    private String gateway;
    private String baseInterface;
    private Integer vlanId;
    private Boolean bonded;
    private String bondName;
    private Integer bondType;
    private String bondOptions;
    private int mtu;
    private boolean bridged;
    private NetworkImplementationDetails networkImplementationDetails;
    private NetworkQoS qos;
    private boolean qosOverridden;
    private Map<String, String> customProperties;

    @ValidNetworkLabelFormat(message = "NETWORK_LABEL_FORMAT_INVALID")
    private Set<String> labels;

    public VdsNetworkInterface() {
        super(new VdsNetworkStatistics(), VdsInterfaceType.NONE.getValue());
    }

    @Override
    @Size(min = 1, max = BusinessEntitiesDefinitions.HOST_NIC_NAME_LENGTH)
    @NotNull(message = "VALIDATION_NAME_NULL")
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
     * Checks whether an interface is part of a bond.
     *
     * @return whether the interface is part of a bond.
     */
    public boolean isBondSlave() {
        return getBondName() != null;
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

    public NetworkImplementationDetails getNetworkImplementationDetails() {
        return networkImplementationDetails;
    }

    public void setNetworkImplementationDetails(NetworkImplementationDetails networkImplementationDetails) {
        this.networkImplementationDetails = networkImplementationDetails;
    }

    /**
     * Gets the QoS configured on this interface, which overrides the one possibly configured on the network.
     */
    public NetworkQoS getQos() {
        return qos;
    }

    /**
     * Sets the QoS configured on this interface, which overrides the one possibly configured on the network.
     */
    public void setQos(NetworkQoS qos) {
        this.qos = qos;
    }

    /**
     * Gets whether QoS overriding is enabled.
     */
    public boolean isQosOverridden() {
        return qosOverridden;
    }

    /**
     * Sets whether QoS overriding is enabled.
     */
    public void setQosOverridden(boolean qosOverridden) {
        this.qosOverridden = qosOverridden;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public boolean hasCustomProperties() {
        return customProperties != null && !customProperties.isEmpty();
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties (Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    /**
     * Preserves nic attributes which are managed by the engine
     *
     * @param sourceNic
     *            the given nic which its attributes should override the one's of the nic
     */
    public void overrideEngineManagedAttributes(VdsNetworkInterface sourceNic) {
        setLabels(sourceNic.getLabels());
        setQosOverridden(sourceNic.isQosOverridden());
        setCustomProperties(sourceNic.getCustomProperties());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(getName())
                .append(" {id=")
                .append(getId())
                .append(", vdsId=")
                .append(getVdsId())
                .append(", macAddress=")
                .append(getMacAddress())
                .append(", networkName=")
                .append(getNetworkName())
                .append(", baseInterface=")
                .append(getBaseInterface())
                .append(", vlanId=")
                .append(getVlanId())
                .append(", bonded=")
                .append(getBonded())
                .append(", bondName=")
                .append(getBondName())
                .append(", bondOptions=")
                .append(getBondOptions())
                .append(", bootProtocol=")
                .append(getBootProtocol())
                .append(", address=")
                .append(getAddress())
                .append(", subnet=")
                .append(getSubnet())
                .append(", gateway=")
                .append(getGateway())
                .append(", mtu=")
                .append(getMtu())
                .append(", bridged=")
                .append(isBridged())
                .append(", speed=")
                .append(getSpeed())
                .append(", type=")
                .append(getType())
                .append(", networkImplementationDetails=")
                .append(getNetworkImplementationDetails())
                .append(", qos=")
                .append(getQos())
                .append(" qosOverridden=")
                .append(isQosOverridden())
                .append(", labels=")
                .append(getLabels())
                .append(", customProperties=")
                .append(getCustomProperties())
                .append("}");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((bondName == null) ? 0 : bondName.hashCode());
        result = prime * result + ((bondOptions == null) ? 0 : bondOptions.hashCode());
        result = prime * result + ((bondType == null) ? 0 : bondType.hashCode());
        result = prime * result + ((bonded == null) ? 0 : bonded.hashCode());
        result = prime * result + ((bootProtocol == null) ? 0 : bootProtocol.hashCode());
        result = prime * result + ((networkName == null) ? 0 : networkName.hashCode());
        result = prime * result + (bridged ? 1231 : 1237);
        result = prime * result + ((gateway == null) ? 0 : gateway.hashCode());
        result = prime * result + mtu;
        result = prime * result + ((subnet == null) ? 0 : subnet.hashCode());
        result = prime * result + ((vdsId == null) ? 0 : vdsId.hashCode());
        result = prime * result + ((baseInterface == null) ? 0 : baseInterface.hashCode());
        result = prime * result + ((vlanId == null) ? 0 : vlanId.hashCode());
        result = prime * result + ((qos == null) ? 0 : qos.hashCode());
        result = prime * result + (qosOverridden ? 1231 : 1237);
        result = prime * result + ((labels == null) ? 0 : labels.hashCode());
        result = prime * result + ((customProperties == null) ? 0 : customProperties.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof VdsNetworkInterface)) {
            return false;
        }
        VdsNetworkInterface other = (VdsNetworkInterface) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        if (bondName == null) {
            if (other.bondName != null) {
                return false;
            }
        } else if (!bondName.equals(other.bondName)) {
            return false;
        }
        if (bondOptions == null) {
            if (other.bondOptions != null) {
                return false;
            }
        } else if (!bondOptions.equals(other.bondOptions)) {
            return false;
        }
        if (bondType == null) {
            if (other.bondType != null) {
                return false;
            }
        } else if (!bondType.equals(other.bondType)) {
            return false;
        }
        if (bonded == null) {
            if (other.bonded != null) {
                return false;
            }
        } else if (!bonded.equals(other.bonded)) {
            return false;
        }
        if (bootProtocol != other.bootProtocol) {
            return false;
        }
        if (!ObjectUtils.objectsEqual(networkName, other.networkName)) {
            return false;
        }
        if (bridged != other.bridged) {
            return false;
        }
        if (gateway == null) {
            if (other.gateway != null) {
                return false;
            }
        } else if (!gateway.equals(other.gateway)) {
            return false;
        }
        if (mtu != other.mtu) {
            return false;
        }
        if (subnet == null) {
            if (other.subnet != null) {
                return false;
            }
        } else if (!subnet.equals(other.subnet)) {
            return false;
        }
        if (vdsId == null) {
            if (other.vdsId != null) {
                return false;
            }
        } else if (!vdsId.equals(other.vdsId)) {
            return false;
        }
        if (baseInterface == null) {
            if (other.baseInterface != null) {
                return false;
            }
        } else if (!baseInterface.equals(other.baseInterface)) {
            return false;
        }
        if (vlanId == null) {
            if (other.vlanId != null) {
                return false;
            }
        } else if (!vlanId.equals(other.vlanId)) {
            return false;
        }
        if (!ObjectUtils.objectsEqual(qos, other.qos)) {
            return false;
        }
        if (qosOverridden != other.qosOverridden) {
            return false;
        }
        if (!ObjectUtils.objectsEqual(labels, other.labels)) {
            return false;
        }
        if (!ObjectUtils.objectsEqual(customProperties, other.customProperties)) {
            return false;
        }

        return true;
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
            StringBuilder builder = new StringBuilder();
            builder.append("{inSync=")
                    .append(isInSync())
                    .append(", managed=")
                    .append(isManaged())
                    .append("}");
            return builder.toString();
        }
    }
}
