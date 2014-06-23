package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.Serializable;

public class ForemanDiscoveredHost implements Serializable {
    private static final long serialVersionUID = -6900772579678185173L;
    private String ip;
    private String name;
    private String mac;
    private String last_report;
    private String subnet_name;

    public String getSubnet_name() { return subnet_name; }
    public void setSubnet_name(String subnet_name) { this.subnet_name = subnet_name; }
    public String getLast_report() { return last_report; }
    public void setLast_report(String last_report) { this.last_report = last_report; }
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
