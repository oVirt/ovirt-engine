package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.common.validation.group.UpdateEntity;

public class IpConfiguration implements Serializable {
    private static final long serialVersionUID = -3207405803308009853L;

    @NotNull(groups = { CreateEntity.class, UpdateEntity.class })
    @Size(min = 1,
            max = 1,
            groups = { CreateEntity.class, UpdateEntity.class },
            message = "Currently is supported only one IPv4 address for NIC."
    )
    private List<IPv4Address> iPv4Addresses = new ArrayList<>();

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public List<IPv4Address> getIPv4Addresses() {
        return iPv4Addresses;
    }

    public IPv4Address getPrimaryAddress() {
        if (!hasPrimaryAddressSet()) {
            throw new IllegalStateException("IpConfiguration does not have IPv4 address set.");
        }
        return getIPv4Addresses().get(0);
    }

    public boolean hasPrimaryAddressSet() {
        return iPv4Addresses != null && !iPv4Addresses.isEmpty() && iPv4Addresses.get(0) != null;
    }

    public void setIPv4Addresses(List<IPv4Address> iPv4Addresses) {
        this.iPv4Addresses = iPv4Addresses;
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
        return Objects.equals(iPv4Addresses, other.iPv4Addresses);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(iPv4Addresses);
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("ipv4Addresses", getIPv4Addresses())
                .build();
    }
}
