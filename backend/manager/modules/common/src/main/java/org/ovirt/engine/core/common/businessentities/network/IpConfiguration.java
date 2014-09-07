package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.utils.ValidationUtils;

public class IpConfiguration implements Serializable {
    private static final long serialVersionUID = -3207405803308009853L;

    private NetworkBootProtocol bootProtocol;

    @Pattern(regexp = ValidationUtils.IP_PATTERN, message = "NETWORK_ADDR_IN_STATIC_IP_BAD_FORMAT")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_NETWORK_ADDR_SIZE)
    private String address;

    @Pattern(regexp = ValidationUtils.IP_PATTERN, message = "NETWORK_ADDR_IN_SUBNET_BAD_FORMAT")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_SUBNET_SIZE)
    private String netmask;

    @Pattern(regexp = ValidationUtils.IP_PATTERN, message = "NETWORK_ADDR_IN_GATEWAY_BAD_FORMAT")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_GATEWAY_SIZE)
    private String gateway;

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

    public String getNetmask() {
        return netmask;
    }

    public void setNetmask(String netmask) {
        this.netmask = netmask;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof IpConfiguration))
            return false;
        IpConfiguration that = (IpConfiguration) o;
        return Objects.equals(getBootProtocol(), that.getBootProtocol()) &&
                Objects.equals(getAddress(), that.getAddress()) &&
                Objects.equals(getNetmask(), that.getNetmask()) &&
                Objects.equals(getGateway(), that.getGateway());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBootProtocol(), getAddress(), getNetmask(), getGateway());
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("bootProtocol", getBootProtocol())
                .append("address", getAddress())
                .append("netmask", getNetmask())
                .append("gateway", getGateway())
                .build();
    }
}
