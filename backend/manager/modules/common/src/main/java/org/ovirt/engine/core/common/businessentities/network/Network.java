package org.ovirt.engine.core.common.businessentities.network;

import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Commented;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.validation.annotation.MTU;
import org.ovirt.engine.core.common.validation.annotation.ValidName;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;
import org.ovirt.engine.core.compat.Guid;

public class Network implements IVdcQueryable, BusinessEntity<Guid>, Nameable, Commented {
    private static final long serialVersionUID = 7357288865938773402L;

    private Guid id;

    @Pattern(regexp = "^[-_a-zA-Z0-9]{1,15}$", message = "NETWORK_ILEGAL_NETWORK_NAME", groups = { CreateEntity.class,
            UpdateEntity.class })
    @Size(min = 1, max = BusinessEntitiesDefinitions.NETWORK_NAME_SIZE)
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String description;

    private String comment;

    private Integer type;

    @Pattern(regexp = ValidationUtils.IPV4_PATTERN, message = "IPV4_ADDR_BAD_FORMAT")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_NETWORK_ADDR_SIZE)
    private String addr;

    @Pattern(regexp = ValidationUtils.IPV4_PATTERN, message = "NETWORK_ADDR_IN_SUBNET_BAD_FORMAT")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_SUBNET_SIZE)
    private String subnet;

    @Pattern(regexp = ValidationUtils.IPV4_PATTERN, message = "IPV4_ADDR_GATEWAY_BAD_FORMAT")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_GATEWAY_SIZE)
    private String gateway;

    @Min(value = 0, message = "NETWORK_VLAN_OUT_OF_RANGE", groups = { CreateEntity.class, UpdateEntity.class })
    @Max(value = 4094, message = "NETWORK_VLAN_OUT_OF_RANGE", groups = { CreateEntity.class, UpdateEntity.class })
    private Integer vlanId;

    private boolean stp;

    @NotNull(message = "VALIDATION_STORAGE_POOL_ID_NOT_NULL", groups = { CreateEntity.class, UpdateEntity.class })
    private Guid dataCenterId;

    private NetworkCluster cluster;

    private boolean vmNetwork;

    private ProviderNetwork providedBy;

    @ValidName(message = "NETWORK_LABEL_FORMAT_INVALID", groups = { CreateEntity.class, UpdateEntity.class })
    private String label;

    @MTU
    private int mtu;

    private Guid qosId;

    public Network() {
        vmNetwork = true;
    }

    public Network(String addr, String description, Guid id, String name, String subnet, String gateway, Integer type,
            Integer vlan_id, boolean stp, int mtu, boolean vmNetwork) {
        this();
        this.addr = addr;
        this.description = description;
        this.id = id;
        this.name = name;
        this.subnet = subnet;
        this.gateway = gateway;
        this.type = type;
        this.vlanId = vlan_id;
        this.stp = stp;
        this.mtu = mtu;
        this.vmNetwork = vmNetwork;
    }

    public NetworkCluster getCluster() {
        return cluster;
    }

    public String getAddr() {
        return this.addr;
    }

    public void setAddr(String value) {
        this.addr = value;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String value) {
        this.description = value;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String value) {
        comment = value;
    }

    @Override
    public Guid getId() {
        return this.id;
    }

    @Override
    public void setId(Guid value) {
        this.id = value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getSubnet() {
        return this.subnet;
    }

    public void setSubnet(String value) {
        this.subnet = value;
    }

    public String getGateway() {
        return this.gateway;
    }

    public void setGateway(String value) {
        this.gateway = value;
    }

    public Integer getType() {
        return this.type;
    }

    public void setType(Integer value) {
        this.type = value;
    }

    public Integer getVlanId() {
        return this.vlanId;
    }

    public void setVlanId(Integer value) {
        this.vlanId = value;
    }

    public boolean getStp() {
        return this.stp;
    }

    public void setStp(boolean value) {
        this.stp = value;
    }

    public Guid getDataCenterId() {
        return this.dataCenterId;
    }

    public void setDataCenterId(Guid value) {
        this.dataCenterId = value;
    }

    public void setCluster(NetworkCluster cluster) {
        this.cluster = cluster;
    }

    public ProviderNetwork getProvidedBy() {
        return providedBy;
    }

    public void setProvidedBy(ProviderNetwork providedBy) {
        this.providedBy = providedBy;
    }

    public boolean isExternal() {
        return providedBy != null;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("description", getDescription())
                .append("comment", getComment())
                .append("subnet", getSubnet())
                .append("gateway", getGateway())
                .append("type", getType())
                .append("vlanId", getVlanId())
                .append("stp", getStp())
                .append("dataCenterId", getDataCenterId())
                .append("mtu", getMtu())
                .append("vmNetwork", isVmNetwork())
                .append("cluster", getCluster())
                .append("providedBy", getProvidedBy())
                .append("label", getLabel())
                .append("qosId", getQosId())
                .build();
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                addr,
                // FIXME: remove cluster from hashCode calculation - breaks the tests when working in JDBC template mode
                // cluster,
                description,
                gateway,
                id,
                name,
                dataCenterId,
                stp,
                subnet,
                type,
                vlanId,
                mtu,
                vmNetwork,
                providedBy,
                label,
                qosId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Network)) {
            return false;
        }
        Network other = (Network) obj;
        return Objects.equals(addr, other.addr)
                // FIXME: currently removing cluster from equals, tests are failing
                // && Objects.equals(cluster, other.cluster)
                && Objects.equals(description, other.description)
                && Objects.equals(gateway, other.gateway)
                && Objects.equals(id, other.id)
                && Objects.equals(name, other.name)
                && Objects.equals(dataCenterId, other.dataCenterId)
                && stp == other.stp
                && Objects.equals(subnet, other.subnet)
                && Objects.equals(type, other.type)
                && Objects.equals(vlanId, other.vlanId)
                && mtu == other.mtu
                && vmNetwork == other.vmNetwork
                && Objects.equals(providedBy, other.providedBy)
                && Objects.equals(label, other.label)
                && Objects.equals(qosId, other.qosId);
    }

    public int getMtu() {
        return mtu;
    }

    public void setMtu(int mtu) {
        this.mtu = mtu;
    }

    public boolean isVmNetwork() {
        return vmNetwork;
    }

    public void setVmNetwork(boolean vmNetwork) {
        this.vmNetwork = vmNetwork;
    }

    /**
     * Gets the ID of the QoS entity configured on this network.
     */
    public Guid getQosId() {
        return qosId;
    }

    /**
     * Sets the ID of the QoS entity configured on this network.
     */
    public void setQosId(Guid qosId) {
        this.qosId = qosId;
    }
}
