package org.ovirt.engine.core.common.businessentities.network;

import java.io.Serializable;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        result = prime * result + ((bootProtocol == null) ? 0 : bootProtocol.hashCode());
        result = prime * result + ((gateway == null) ? 0 : gateway.hashCode());
        result = prime * result + ((netmask == null) ? 0 : netmask.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        IpConfiguration other = (IpConfiguration) obj;
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        if (bootProtocol != other.bootProtocol)
            return false;
        if (gateway == null) {
            if (other.gateway != null)
                return false;
        } else if (!gateway.equals(other.gateway))
            return false;
        if (netmask == null) {
            if (other.netmask != null)
                return false;
        } else if (!netmask.equals(other.netmask))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "IpConfiguration [bootProtocol=" + bootProtocol
                + ", address=" + address
                + ", netmask=" + netmask
                + ", gateway=" + gateway + "]";
    }
}
