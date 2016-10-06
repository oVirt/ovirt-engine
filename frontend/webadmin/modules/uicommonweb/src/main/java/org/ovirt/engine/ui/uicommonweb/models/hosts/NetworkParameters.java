package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;

public class NetworkParameters implements InterfacePropertiesAccessor {

    private Ipv4BootProtocol ipv4BootProtocol;
    private String ipv4Address;
    private String ipv4Netmask;
    private String ipv4Gateway;
    private Ipv6BootProtocol ipv6BootProtocol;
    private String ipv6Address;
    private Integer ipv6Prefix;
    private String ipv6Gateway;
    private boolean qosOverridden;
    private HostNetworkQos qos;
    private Map<String, String> customProperties;
    private DnsResolverConfiguration dnsResolverConfiguration;

    public NetworkParameters() {
    }

    @Override
    public Ipv4BootProtocol getIpv4BootProtocol() {
        return ipv4BootProtocol;
    }

    public void setIpv4BootProtocol(Ipv4BootProtocol ipv4BootProtocol) {
        this.ipv4BootProtocol = ipv4BootProtocol;
    }

    @Override
    public String getIpv4Address() {
        return ipv4Address;
    }

    public void setIpv4Address(String ipv4Address) {
        this.ipv4Address = ipv4Address;
    }

    @Override
    public String getIpv4Netmask() {
        return ipv4Netmask;
    }

    public void setIpv4Netmask(String ipv4Netmask) {
        this.ipv4Netmask = ipv4Netmask;
    }

    @Override
    public String getIpv4Gateway() {
        return ipv4Gateway;
    }
    public void setIpv4Gateway(String ipv4Gateway) {
        this.ipv4Gateway = ipv4Gateway;
    }

    @Override
    public Ipv6BootProtocol getIpv6BootProtocol() {
        return ipv6BootProtocol;
    }

    public void setIpv6BootProtocol(Ipv6BootProtocol ipv6BootProtocol) {
        this.ipv6BootProtocol = ipv6BootProtocol;
    }

    @Override
    public String getIpv6Address() {
        return ipv6Address;
    }

    public void setIpv6Address(String ipv6Address) {
        this.ipv6Address = ipv6Address;
    }

    @Override
    public String getIpv6Gateway() {
        return ipv6Gateway;
    }

    public void setIpv6Gateway(String ipv6Gateway) {
        this.ipv6Gateway = ipv6Gateway;
    }

    @Override
    public Integer getIpv6Prefix() {
        return ipv6Prefix;
    }

    public void setIpv6Prefix(Integer ipv6Prefix) {
        this.ipv6Prefix = ipv6Prefix;
    }

    public boolean isQosOverridden() {
        return qosOverridden;
    }

    public void setQosOverridden(boolean qosOverridden) {
        this.qosOverridden = qosOverridden;
    }

    @Override
    public HostNetworkQos getHostNetworkQos() {
        return qos;
    }

    public void setHostNetworkQos(HostNetworkQos qos) {
        this.qos = qos;
    }

    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    public DnsResolverConfiguration getDnsResolverConfiguration() {
        return dnsResolverConfiguration;
    }

    public void setDnsResolverConfiguration(DnsResolverConfiguration dnsResolverConfiguration) {
        this.dnsResolverConfiguration = dnsResolverConfiguration;
    }
}
