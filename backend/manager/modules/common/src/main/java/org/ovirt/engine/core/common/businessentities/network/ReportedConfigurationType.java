package org.ovirt.engine.core.common.businessentities.network;

public enum ReportedConfigurationType {
    MTU("mtu"),
    BRIDGED("bridged"),
    VLAN("vlan"),
    BOOT_PROTOCOL("boot_protocol"),
    IP_ADDRESS("ip_address"),
    NETMASK("netmask"),
    GATEWAY("gateway"),
    OUT_AVERAGE_LINK_SHARE("outAverageLinkShare"),
    OUT_AVERAGE_UPPER_LIMIT("outAverageUpperLimit"),
    OUT_AVERAGE_REAL_TIME("outAverageRealTime");

    private final String name;

    ReportedConfigurationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
