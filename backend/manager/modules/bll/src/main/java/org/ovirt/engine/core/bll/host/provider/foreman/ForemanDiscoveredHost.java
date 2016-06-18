package org.ovirt.engine.core.bll.host.provider.foreman;
import java.io.Serializable;
import org.codehaus.jackson.annotate.JsonProperty;

public class ForemanDiscoveredHost implements Serializable {
    private static final long serialVersionUID = -6900772579678185173L;
    private String ip;
    private String name;
    private String mac;
    @JsonProperty("last_report")
    private String lastReport;
    @JsonProperty("subnet_name")
    private String subnetName;

    public String getSubnetName() {
        return subnetName;
    }

    public void setSubnetName(String subnetName) {
        this.subnetName = subnetName;
    }

    public String getLastReport() {
        return lastReport;
    }

    public void setLastReport(String lastReport) {
        this.lastReport = lastReport;
    }

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
