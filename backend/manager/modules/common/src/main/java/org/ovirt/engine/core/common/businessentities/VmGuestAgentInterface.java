package org.ovirt.engine.core.common.businessentities;

import java.util.List;

import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class VmGuestAgentInterface extends IVdcQueryable {

    private static final long serialVersionUID = -9164680367965630250L;

    /**
     * The Id of the VM this interface belongs to
     */
    private Guid vmId;

    /**
     * The internal nic's name as seen by the guest agent
     */
    private String interfaceName;

    /**
     * The internal nic's mac address as seen by the guest agent
     */
    private String macAddress;

    /**
     * The vNic's IPv4 addresses
     */
    private List<String> ipv4Addresses;

    /**
     * The vNic's IPv6 addresses
     */
    private List<String> ipv6Addresses;

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public List<String> getIpv4Addresses() {
        return ipv4Addresses;
    }

    public void setIpv4Addresses(List<String> ipv4Addresses) {
        this.ipv4Addresses = ipv4Addresses;
    }

    public List<String> getIpv6Addresses() {
        return ipv6Addresses;
    }

    public void setIpv6Addresses(List<String> ipv6Addresses) {
        this.ipv6Addresses = ipv6Addresses;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getInterfaceName() == null) ? 0 : getInterfaceName().hashCode());
        result = prime * result + ((getIpv4Addresses() == null) ? 0 : getIpv4Addresses().hashCode());
        result = prime * result + ((getIpv6Addresses() == null) ? 0 : getIpv6Addresses().hashCode());
        result = prime * result + ((getMacAddress() == null) ? 0 : getMacAddress().hashCode());
        result = prime * result + ((getVmId() == null) ? 0 : getVmId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof VmGuestAgentInterface)) {
            return false;
        }
        VmGuestAgentInterface other = (VmGuestAgentInterface) obj;
        return (ObjectUtils.objectsEqual(getInterfaceName(), other.getInterfaceName())
                && ObjectUtils.objectsEqual(getIpv4Addresses(), other.getIpv4Addresses())
                && ObjectUtils.objectsEqual(getIpv6Addresses(), other.getIpv6Addresses())
                && ObjectUtils.objectsEqual(getMacAddress(), other.getMacAddress())
                && ObjectUtils.objectsEqual(getVmId(), other.getVmId()));
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("vmId", getVmId())
                .append("macAddress", getMacAddress())
                .append("ipv4", getIpv4Addresses())
                .append("ipv6", getIpv6Addresses())
                .build();
    }
}
