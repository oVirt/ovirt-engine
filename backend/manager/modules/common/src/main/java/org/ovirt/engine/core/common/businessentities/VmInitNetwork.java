package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.compat.Guid;

public class VmInitNetwork implements Serializable, BusinessEntity<Guid> {
    private static final long serialVersionUID = 1257388375049806155L;
    private Boolean startOnBoot;
    private String name;

    private Ipv4BootProtocol bootProtocol;
    private String ip;
    private String netmask;
    private String gateway;

    private Ipv6BootProtocol ipv6BootProtocol;
    private String ipv6Address;
    private Integer ipv6Prefix;
    private String ipv6Gateway;

    private Guid id;

    public VmInitNetwork() {
        this.bootProtocol = Ipv4BootProtocol.NONE;
        this.ipv6BootProtocol = Ipv6BootProtocol.NONE;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public void setStartOnBoot(Boolean startOnBoot) {
        this.startOnBoot = startOnBoot;
    }
    public Boolean getStartOnBoot() {
        return startOnBoot;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public Ipv4BootProtocol getBootProtocol() {
        return bootProtocol;
    }
    public void setBootProtocol(Ipv4BootProtocol bootProtocol) {
        this.bootProtocol = (bootProtocol == null) ? Ipv4BootProtocol.NONE : bootProtocol;
    }
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
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
    public Ipv6BootProtocol getIpv6BootProtocol() {
        return ipv6BootProtocol;
    }
    public void setIpv6BootProtocol(Ipv6BootProtocol ipv6BootProtocol) {
        this.ipv6BootProtocol = (ipv6BootProtocol == null) ? Ipv6BootProtocol.NONE : ipv6BootProtocol;
    }
    public String getIpv6Address() {
        return ipv6Address;
    }
    public void setIpv6Address(String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }
    public Integer getIpv6Prefix() {
        return ipv6Prefix;
    }
    public void setIpv6Prefix(Integer ipv6Prefix) {
        this.ipv6Prefix = ipv6Prefix;
    }
    public String getIpv6Gateway() {
        return ipv6Gateway;
    }
    public void setIpv6Gateway(String ipv6Gateway) {
        this.ipv6Gateway = ipv6Gateway;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                startOnBoot,
                name,
                bootProtocol,
                ip,
                netmask,
                gateway,
                ipv6BootProtocol,
                ipv6Address,
                ipv6Prefix,
                ipv6Gateway
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmInitNetwork)) {
            return false;
        }
        VmInitNetwork other = (VmInitNetwork) obj;
        return Objects.equals(id, other.id) &&
                Objects.equals(startOnBoot, other.startOnBoot) &&
                Objects.equals(name, other.name) &&
                Objects.equals(bootProtocol, other.bootProtocol) &&
                Objects.equals(ip, other.ip) &&
                Objects.equals(netmask, other.netmask) &&
                Objects.equals(gateway, other.gateway) &&
                Objects.equals(ipv6BootProtocol, other.ipv6BootProtocol) &&
                Objects.equals(ipv6Address, other.ipv6Address) &&
                Objects.equals(ipv6Prefix, other.ipv6Prefix) &&
                Objects.equals(ipv6Gateway, other.ipv6Gateway);
    }
}
