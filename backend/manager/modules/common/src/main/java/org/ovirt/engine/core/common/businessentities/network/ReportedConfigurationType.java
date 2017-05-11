package org.ovirt.engine.core.common.businessentities.network;

public enum ReportedConfigurationType {
    MTU("mtu"),
    BRIDGED("bridged"),
    VLAN("vlan"),
    IPV4_BOOT_PROTOCOL("ipv4_boot_protocol"),
    IPV4_ADDRESS("ipv4_address"),
    IPV4_NETMASK("ipv4_netmask"),
    IPV4_GATEWAY("ipv4_gateway"),
    IPV6_BOOT_PROTOCOL("ipv6_boot_protocol"),
    IPV6_ADDRESS("ipv6_address"),
    IPV6_PREFIX("ipv6_prefix"),
    IPV6_GATEWAY("ipv6_gateway"),
    OUT_AVERAGE_LINK_SHARE("outAverageLinkShare"),
    OUT_AVERAGE_UPPER_LIMIT("outAverageUpperLimit"),
    OUT_AVERAGE_REAL_TIME("outAverageRealTime"),
    SWITCH_TYPE("switchType"),
    DNS_CONFIGURATION("dns_configuration"),
    DEFAULT_ROUTE("default_route");

    private final String name;

    ReportedConfigurationType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
