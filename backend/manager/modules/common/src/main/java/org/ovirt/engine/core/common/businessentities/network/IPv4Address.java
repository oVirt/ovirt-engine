package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.Ipv4;
import org.ovirt.engine.core.common.validation.annotation.Mask;

/**
 * To represent no address, no gateway or no netmask use {@code null};
 * Do NOT use an empty string.
 */
public class IPv4Address implements Serializable {
    private static final long serialVersionUID = -3762999158282212711L;

    @Ipv4(message = "IPV4_ADDR_BAD_FORMAT")
    private String address;

    @Mask
    private String netmask;

    @Ipv4(message = "IPV4_ADDR_GATEWAY_BAD_FORMAT")
    private String gateway;

    private Ipv4BootProtocol bootProtocol;

    public Ipv4BootProtocol getBootProtocol() {
        return bootProtocol;
    }

    public void setBootProtocol(Ipv4BootProtocol bootProtocol) {
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
