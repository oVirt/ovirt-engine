package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;

public class IpConfiguration implements Serializable {
    private static final long serialVersionUID = -3207405803308009853L;

    @Valid
    @NotNull(groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = 1,
            groups = { CreateEntity.class, UpdateEntity.class },
            message = "Only one IPv4 address is supported for a network attachment.")
    private List<IPv4Address> iPv4Addresses = new ArrayList<>();

    @Valid
    @NotNull(groups = { CreateEntity.class, UpdateEntity.class })
    @Size(max = 1,
            groups = { CreateEntity.class, UpdateEntity.class },
            message = "Only one IPv6 address is supported for a network attachment.")
    private List<IpV6Address> ipV6Addresses = new ArrayList<>();

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public List<IPv4Address> getIPv4Addresses() {
        return iPv4Addresses;
    }

    public IPv4Address getIpv4PrimaryAddress() {
        if (!hasIpv4PrimaryAddressSet()) {
            throw new IllegalStateException("IpConfiguration does not have IPv4 address set.");
        }
        return getIPv4Addresses().get(0);
    }

    public IpV6Address getIpv6PrimaryAddress() {
        if (!hasIpv6PrimaryAddressSet()) {
            throw new IllegalStateException("IpConfiguration does not have IPv6 address set.");
        }
        return getIpV6Addresses().get(0);
    }

    public boolean hasIpv4PrimaryAddressSet() {
        return iPv4Addresses != null && !iPv4Addresses.isEmpty() && iPv4Addresses.get(0) != null;
    }

    public boolean hasIpv6PrimaryAddressSet() {
        return ipV6Addresses != null && !ipV6Addresses.isEmpty() && ipV6Addresses.get(0) != null;
    }

    public void setIPv4Addresses(List<IPv4Address> iPv4Addresses) {
        this.iPv4Addresses = iPv4Addresses;
    }

    public List<IpV6Address> getIpV6Addresses() {
        return ipV6Addresses;
    }

    public void setIpV6Addresses(List<IpV6Address> ipV6Addresses) {
        this.ipV6Addresses = ipV6Addresses;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof IpConfiguration)) {
            return false;
        }
        IpConfiguration other = (IpConfiguration) o;
        return Objects.equals(iPv4Addresses, other.iPv4Addresses)
                && Objects.equals(ipV6Addresses, other.ipV6Addresses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iPv4Addresses, ipV6Addresses);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("ipv4Addresses", getIPv4Addresses())
                .append("ipv6Addresses", getIpV6Addresses())
                .build();
    }
}
