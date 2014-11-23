package org.ovirt.engine.core.common.businessentities;

public class ExternalDiscoveredHost implements ExternalEntityBase {
    private static final long serialVersionUID = -6900772579678185173L;
    private String ip;
    private String mac;
    private String name;
    private String lastReport;
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

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return "MAC: " + (mac != null ? mac : "[N/A]") + "\n" +
                " | DiscoverTime : " + (lastReport != null ? lastReport : "[N/A]") + "\n" +
                " | Subnet: " +  (subnetName != null ? subnetName : "[N/A]") + "\n" +
                " | IP: " + (ip != null ? ip : "[N/A]");
    }
}
