package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.compat.Guid;

public class VmInitNetwork implements Serializable, BusinessEntity<Guid> {
    private static final long serialVersionUID = 1257388375049806155L;
    private Boolean startOnBoot;
    private String name;
    private Ipv4BootProtocol bootProtocol;
    private String ip;
    private String netmask;
    private String gateway;
    private Guid id;

    public VmInitNetwork() {
        this.bootProtocol = Ipv4BootProtocol.NONE;
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
}
