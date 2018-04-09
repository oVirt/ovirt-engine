package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.Objects;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.annotation.Ipv6;

/**
 * To represent no address or no gateway use {@code null};
 * Do NOT use an empty string.
 */
public class IpV6Address implements Serializable {
    private static final long serialVersionUID = 5112759833343439658L;

    @Ipv6
    private String address;

    @Min(0L)
    @Max(128L)
    private Integer prefix;

    @Ipv6
    private String gateway;

    private Ipv6BootProtocol bootProtocol;

    public Ipv6BootProtocol getBootProtocol() {
        return bootProtocol;
    }

    public void setBootProtocol(Ipv6BootProtocol bootProtocol) {
        this.bootProtocol = bootProtocol;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGateway() {
        return gateway;
    }

    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    public Integer getPrefix() {
        return prefix;
    }

    public void setPrefix(Integer prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IpV6Address)) {
            return false;
        }
        IpV6Address other = (IpV6Address) o;
        return Objects.equals(address, other.address)
                && Objects.equals(prefix, other.prefix)
                && Objects.equals(gateway, other.gateway)
                && Objects.equals(bootProtocol, other.bootProtocol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                address,
                prefix,
                gateway,
                bootProtocol
        );
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("address", getAddress())
                .append("prefix", getPrefix())
                .append("gateway", getGateway())
                .append("bootProtocol", getBootProtocol())
                .build();
    }
}
