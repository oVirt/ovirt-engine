package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

public class ExternalDiscoveredHost implements Serializable {
    private static final long serialVersionUID = -6900772579678185173L;
    private String ip;
    private String mac;
    private String name;

    public String getMac() {
        return mac;
    }
    public void setMac(String mac) {
        this.mac = mac;
    }
    public String getIp() {
        return ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
