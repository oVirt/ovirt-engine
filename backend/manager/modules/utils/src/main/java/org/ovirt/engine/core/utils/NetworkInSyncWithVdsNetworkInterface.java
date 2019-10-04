package org.ovirt.engine.core.utils;

import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.DEFAULT_ROUTE;
import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.DNS_CONFIGURATION;
import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE;
import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.OUT_AVERAGE_REAL_TIME;
import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT;
import static org.ovirt.engine.core.utils.network.predicate.IpUnspecifiedPredicate.ipUnspecifiedPredicate;
import static org.ovirt.engine.core.utils.network.predicate.IsDefaultRouteOnInterfacePredicate.isDefaultRouteOnInterfacePredicate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.DnsResolverConfiguration;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.NameServer;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.SubnetUtils;
import org.ovirt.engine.core.utils.network.predicate.IpAddressPredicate;

public class NetworkInSyncWithVdsNetworkInterface {

    private final VdsNetworkInterface iface;
    private final Network network;
    private final NetworkAttachment networkAttachment;
    private final HostNetworkQos ifaceQos;
    private final boolean isDefaultRouteNetwork;
    private final HostNetworkQos hostNetworkQos;
    private final DnsResolverConfiguration reportedDnsResolverConfiguration;
    private final Cluster cluster;

    public NetworkInSyncWithVdsNetworkInterface(VdsNetworkInterface iface,
            Network network,
            HostNetworkQos hostNetworkQos,
            NetworkAttachment networkAttachment,
            DnsResolverConfiguration reportedDnsResolverConfiguration,
            Cluster cluster,
            boolean isDefaultRouteNetwork) {
        this.iface = iface;
        this.network = network;
        this.ifaceQos = iface.getQos();
        this.isDefaultRouteNetwork = isDefaultRouteNetwork;
        this.hostNetworkQos = hostNetworkQos;
        this.networkAttachment = networkAttachment;
        this.reportedDnsResolverConfiguration = reportedDnsResolverConfiguration;
        this.cluster = cluster;
    }

    public boolean isNetworkInSync() {
        return reportConfigurationsOnHost().isNetworkInSync();
    }

    public ReportedConfigurations reportConfigurationsOnHost () {
        ReportedConfigurations result = new ReportedConfigurations();

        result.add(ReportedConfigurationType.MTU, iface.getMtu(), NetworkUtils.getHostMtuActualValue(network),
                isNetworkMtuInSync());
        result.add(ReportedConfigurationType.BRIDGED, iface.isBridged(), network.isVmNetwork());
        result.add(ReportedConfigurationType.VLAN, iface.getVlanId(), network.getVlanId());
        result.add(ReportedConfigurationType.SWITCH_TYPE,
                iface.getReportedSwitchType(),
                cluster.getRequiredSwitchTypeForCluster());

        addReportedIpv4Configuration(result);
        addReportedIpv6Configuration(result);

        boolean reportHostQos = ifaceQos != null || hostNetworkQos != null;
        if (reportHostQos) {
            result.add(OUT_AVERAGE_LINK_SHARE, getOutAverageLinkshare(ifaceQos), getOutAverageLinkshare(hostNetworkQos));
            result.add(OUT_AVERAGE_UPPER_LIMIT, getOutAverageUpperlimit(ifaceQos), getOutAverageUpperlimit(hostNetworkQos));
            result.add(OUT_AVERAGE_REAL_TIME, getOutAverageRealtime(ifaceQos), getOutAverageRealtime(hostNetworkQos));
        }

        addDnsConfiguration(result);
        addReportedDefaultRouteConfiguration(result);

        return result;
    }

    private void addReportedDefaultRouteConfiguration(ReportedConfigurations result) {
        boolean isDefaultRouteInterface = isDefaultRouteOnInterfacePredicate().test(iface);
        result.add(DEFAULT_ROUTE,
                isDefaultRouteInterface,
                isDefaultRouteNetwork,
                Objects.equals(isDefaultRouteInterface, isDefaultRouteNetwork));
    }

    private boolean isNetworkMtuInSync() {
        return iface.getMtu() == NetworkUtils.getHostMtuActualValue(network);
    }

    private boolean isIpv4NetworkSubnetInSync() {
        return getsSubnetUtilsInstance().equalSubnet(iface.getIpv4Subnet(), getIpv4PrimaryAddress().getNetmask());
    }

    private boolean isIpv4GatewayInSync() {
        String gatewayDesiredValue = getIpv4PrimaryAddress().getGateway();
        String gatewayActualValue = iface.getIpv4Gateway();
        return Objects.equals(gatewayDesiredValue, gatewayActualValue);
    }

    private boolean isIpv6PrefixInSync() {
        return Objects.equals(iface.getIpv6Prefix(), getIpv6PrimaryAddress().getPrefix());
    }

    private boolean isIpv6AddressInSync(String attachmentValue, String ifaceValue) {
        IpAddressPredicate p = new IpAddressPredicate(ifaceValue);
        return p.test(attachmentValue) || (ipUnspecifiedPredicate().test(ifaceValue) && attachmentValue == null);
    }

    private SubnetUtils getsSubnetUtilsInstance() {
        return SubnetUtils.getInstance();
    }

    private static Integer getOutAverageRealtime(HostNetworkQos qos) {
        return qos == null ? null : qos.getOutAverageRealtime();
    }

    private static Integer getOutAverageUpperlimit(HostNetworkQos qos) {
        return qos == null ? null : qos.getOutAverageUpperlimit();
    }

    private static Integer getOutAverageLinkshare(HostNetworkQos qos) {
        return qos == null ? null : qos.getOutAverageLinkshare();
    }

    private boolean isIpv4PrimaryAddressExist() {
        IpConfiguration networkDataCenterIpConfigurationDefinition = getIpConfigurationOfNetworkAttachment();
        return networkDataCenterIpConfigurationDefinition != null
                && networkDataCenterIpConfigurationDefinition.hasIpv4PrimaryAddressSet();
    }

    @SuppressWarnings("ConstantConditions")
    private IPv4Address getIpv4PrimaryAddress() {
        return getIpConfigurationOfNetworkAttachment().getIpv4PrimaryAddress();
    }

    private boolean isIpv6PrimaryAddressExist() {
        IpConfiguration ipConfiguration = getIpConfigurationOfNetworkAttachment();
        return ipConfiguration != null
                && ipConfiguration.hasIpv6PrimaryAddressSet();
    }

    @SuppressWarnings("ConstantConditions")
    private IpV6Address getIpv6PrimaryAddress() {
        return getIpConfigurationOfNetworkAttachment().getIpv6PrimaryAddress();
    }

    private IpConfiguration getIpConfigurationOfNetworkAttachment() {
        return networkAttachment == null ? null : networkAttachment.getIpConfiguration();
    }

    private void addReportedIpv4Configuration(ReportedConfigurations result) {
        if (!isIpv4PrimaryAddressExist()) {
            return;
        }
        Ipv4BootProtocol definedIpv4BootProtocol = getIpv4PrimaryAddress().getBootProtocol();
        result.add(ReportedConfigurationType.IPV4_BOOT_PROTOCOL, iface.getIpv4BootProtocol(), definedIpv4BootProtocol);

        if (definedIpv4BootProtocol == Ipv4BootProtocol.STATIC_IP && iface.getIpv4BootProtocol() == definedIpv4BootProtocol) {
            result.add(ReportedConfigurationType.IPV4_NETMASK,
                    iface.getIpv4Subnet(), getIpv4PrimaryAddress().getNetmask(), isIpv4NetworkSubnetInSync());
            result.add(ReportedConfigurationType.IPV4_ADDRESS,
                    iface.getIpv4Address(), getIpv4PrimaryAddress().getAddress());
            result.add(ReportedConfigurationType.IPV4_GATEWAY,
                    iface.getIpv4Gateway(), getIpv4PrimaryAddress().getGateway(), isIpv4GatewayInSync());
        }
    }

    private void addReportedIpv6Configuration(ReportedConfigurations result) {
        if (!isIpv6PrimaryAddressExist()) {
            return;
        }
        Ipv6BootProtocol definedIpv6BootProtocol = getIpv6PrimaryAddress().getBootProtocol();
        result.add(ReportedConfigurationType.IPV6_BOOT_PROTOCOL, iface.getIpv6BootProtocol(), definedIpv6BootProtocol);

        if (definedIpv6BootProtocol == Ipv6BootProtocol.STATIC_IP
                && iface.getIpv6BootProtocol() == definedIpv6BootProtocol) {
            result.add(ReportedConfigurationType.IPV6_PREFIX,
                    iface.getIpv6Prefix(),
                    getIpv6PrimaryAddress().getPrefix(),
                    isIpv6PrefixInSync());
            result.add(ReportedConfigurationType.IPV6_ADDRESS,
                    iface.getIpv6Address(),
                    getIpv6PrimaryAddress().getAddress(),
                    isIpv6AddressInSync(getIpv6PrimaryAddress().getAddress(), iface.getIpv6Address()));
            result.add(ReportedConfigurationType.IPV6_GATEWAY,
                    iface.getIpv6Gateway(),
                    getIpv6PrimaryAddress().getGateway(),
                    isIpv6AddressInSync(getIpv6PrimaryAddress().getGateway(), iface.getIpv6Gateway()));
        }
    }

    private void addDnsConfiguration(ReportedConfigurations result) {
        if (isDefaultRouteNetwork) {
            List<NameServer> nameServersOfHost = getNameServers(reportedDnsResolverConfiguration);
            List<NameServer> expectedNameServers = getExpectedNameServers();
            result.add(DNS_CONFIGURATION,
                    addressesAsString(nameServersOfHost),
                    addressesAsString(expectedNameServers),
                    nameServersOfHost.containsAll(expectedNameServers));
        }
    }

    private List<NameServer> getExpectedNameServers() {
        List<NameServer> nameServersOfNetworkAttachment =
                getNameServers(networkAttachment.getDnsResolverConfiguration());

        List<NameServer> nameServersOfNetwork = getNameServers(network.getDnsResolverConfiguration());

        return nameServersOfNetworkAttachment.isEmpty() ? nameServersOfNetwork : nameServersOfNetworkAttachment;
    }

    private String addressesAsString(List<NameServer> nameServers) {
        if (nameServers.isEmpty()) {
            return null;
        }
        return nameServers.stream().map(NameServer::getAddress).sorted().collect(Collectors.joining(","));
    }

    private List<NameServer> getNameServers(DnsResolverConfiguration dnsResolverConfiguration) {
        if (dnsResolverConfiguration == null) {
            return Collections.emptyList();
        }

        List<NameServer> nameServers = dnsResolverConfiguration.getNameServers();
        if (nameServers == null || nameServers.isEmpty()) {
            return Collections.emptyList();
        }

        return nameServers;
    }
}
