package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.utils.ValidationUtils;

public class IPv4Address implements Serializable {
    private static final long serialVersionUID = -3762999158282212711L;

    @Pattern(regexp = ValidationUtils.IP_PATTERN, message = "NETWORK_ADDR_IN_STATIC_IP_BAD_FORMAT")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_NETWORK_ADDR_SIZE)
    private String address;

    @Pattern(regexp = ValidationUtils.IP_PATTERN, message = "NETWORK_ADDR_IN_SUBNET_BAD_FORMAT")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_SUBNET_SIZE)
    private String netmask;

    @Pattern(regexp = ValidationUtils.IP_PATTERN, message = "NETWORK_ADDR_IN_GATEWAY_BAD_FORMAT")
    @Size(max = BusinessEntitiesDefinitions.GENERAL_GATEWAY_SIZE)
    private String gateway;

    private NetworkBootProtocol bootProtocol;

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
        if (this == o) {
            return true;
        }
        if (!(o instanceof IPv4Address)) {
            return false;
        }
        IPv4Address other = (IPv4Address) o;
        return Objects.equals(address, other.address)
                && Objects.equals(netmask, other.netmask)
                && Objects.equals(gateway, other.gateway)
                && Objects.equals(bootProtocol, other.bootProtocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                address,
                netmask,
                gateway,
                bootProtocol
        );
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("address", getAddress())
                .append("netmask", getNetmask())
                .append("gateway", getGateway())
                .append("bootProtocol", getBootProtocol())
                .build();
    }
}
