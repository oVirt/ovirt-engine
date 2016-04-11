package org.ovirt.engine.core.utils;

import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE;
import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.OUT_AVERAGE_REAL_TIME;
import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.IpV6Address;
import org.ovirt.engine.core.common.businessentities.network.Ipv4BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Ipv6BootProtocol;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkAttachment;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.SubnetUtils;

public class NetworkInSyncWithVdsNetworkInterface {

    private final VdsNetworkInterface iface;
    private final Network network;
    private final NetworkAttachment networkAttachment;
    private final HostNetworkQos ifaceQos;
    private final HostNetworkQos hostNetworkQos;
    private final Cluster cluster;

    public NetworkInSyncWithVdsNetworkInterface(VdsNetworkInterface iface,
            Network network,
            HostNetworkQos hostNetworkQos,
            NetworkAttachment networkAttachment,
            Cluster cluster) {
        this.iface = iface;
        this.network = network;
        this.ifaceQos = iface.getQos();
        this.hostNetworkQos = hostNetworkQos;
        this.networkAttachment = networkAttachment;
        this.cluster = cluster;
    }

    public boolean isNetworkInSync() {
        return reportConfigurationsOnHost().isNetworkInSync();
    }

    public ReportedConfigurations reportConfigurationsOnHost () {
        ReportedConfigurations result = new ReportedConfigurations();

        Integer networkMtu = network.getMtu() == 0 ? NetworkUtils.getDefaultMtu() : network.getMtu();
        result.add(ReportedConfigurationType.MTU, iface.getMtu(), networkMtu, isNetworkMtuInSync());
        result.add(ReportedConfigurationType.BRIDGED, iface.isBridged(), network.isVmNetwork());
        result.add(ReportedConfigurationType.VLAN, iface.getVlanId(), network.getVlanId());
        result.add(ReportedConfigurationType.SWITCH_TYPE,
                iface.getReportedSwitchType(),
                cluster.getRequiredSwitchTypeForCluster());

        addReportedIpv4Configuration(result);

        /**
         * TODO: YZ - uncomment the method call after v4.0 is branched out.
         *
         * Reporting out-of-sync IPv6 configuration is disabled temporary.
         * It's planned to be re-enabled after v4.0-beta is released.
         *
         * addReportedIpv6Configuration(result);
         */

        boolean reportHostQos = ifaceQos != null || hostNetworkQos != null;
        if (reportHostQos) {
            result.add(OUT_AVERAGE_LINK_SHARE, getOutAverageLinkshare(ifaceQos), getOutAverageLinkshare(hostNetworkQos));
            result.add(OUT_AVERAGE_UPPER_LIMIT, getOutAverageUpperlimit(ifaceQos), getOutAverageUpperlimit(hostNetworkQos));
            result.add(OUT_AVERAGE_REAL_TIME, getOutAverageRealtime(ifaceQos), getOutAverageRealtime(hostNetworkQos));
        }

        return result;
    }

    private boolean isNetworkMtuInSync() {
        boolean networkValueSetToDefaultMtu = network.getMtu() == 0;
        boolean ifaceValueSetToDefaultMtu = iface.getMtu() == NetworkUtils.getDefaultMtu();
        boolean bothUsesDefaultValue = networkValueSetToDefaultMtu && ifaceValueSetToDefaultMtu;
        return bothUsesDefaultValue || iface.getMtu() == network.getMtu();
    }

    private boolean isIpv4NetworkSubnetInSync() {
        return getsSubnetUtilsInstance().equalSubnet(iface.getIpv4Subnet(), getIpv4PrimaryAddress().getNetmask());
    }

    private boolean isIpv4GatewayInSync() {
        String gatewayDesiredValue = getIpv4PrimaryAddress().getGateway();
        String gatewayActualValue = iface.getIpv4Gateway();
        boolean bothBlank = StringUtils.isBlank(gatewayDesiredValue) && StringUtils.isBlank(gatewayActualValue);
        return bothBlank || Objects.equals(gatewayDesiredValue, gatewayActualValue);
    }

    private boolean isIpv6PrefixInSync() {
        return Objects.equals(iface.getIpv6Prefix(), getIpv6PrimaryAddress().getPrefix());
    }

    private boolean isIpv6GatewayInSync() {
        String gatewayDesiredValue = getIpv6PrimaryAddress().getGateway();
        String gatewayActualValue = iface.getIpv6Gateway();
        boolean bothBlank = StringUtils.isBlank(gatewayDesiredValue) && StringUtils.isBlank(gatewayActualValue);
        return bothBlank || Objects.equals(gatewayDesiredValue, gatewayActualValue);
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
                    getIpv6PrimaryAddress().getAddress());
            result.add(ReportedConfigurationType.IPV6_GATEWAY,
                    iface.getIpv6Gateway(),
                    getIpv6PrimaryAddress().getGateway(),
                    isIpv6GatewayInSync());
        }
    }
}
