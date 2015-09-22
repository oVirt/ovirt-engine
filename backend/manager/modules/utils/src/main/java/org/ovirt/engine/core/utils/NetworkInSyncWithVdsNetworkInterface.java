package org.ovirt.engine.core.utils;

import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE;
import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.OUT_AVERAGE_REAL_TIME;
import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.IPv4Address;
import org.ovirt.engine.core.common.businessentities.network.IpConfiguration;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkBootProtocol;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.ObjectUtils;

public class NetworkInSyncWithVdsNetworkInterface {

    private final VdsNetworkInterface iface;
    private final Network network;
    private final HostNetworkQos ifaceQos;
    private final HostNetworkQos hostNetworkQos;
    private final IpConfiguration networkDataCenterIpConfigurationDefinition;

    public NetworkInSyncWithVdsNetworkInterface(VdsNetworkInterface iface,
            Network network,
            HostNetworkQos hostNetworkQos,
            IpConfiguration networkDataCenterIpConfigurationDefinition) {
        this.iface = iface;
        this.network = network;
        this.ifaceQos = iface.getQos();
        this.hostNetworkQos = hostNetworkQos;
        this.networkDataCenterIpConfigurationDefinition = networkDataCenterIpConfigurationDefinition;
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

        addReportedIpConfiguration(result);

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
        return (bothUsesDefaultValue || iface.getMtu() == network.getMtu());
    }

    public boolean qosParametersEqual() {
        if (ifaceQos == hostNetworkQos) {
            return true;
        }

        boolean ifaceQosUnset = qosUnset(ifaceQos);
        boolean hostNetworkQosUnset = qosUnset(hostNetworkQos);

        if (ifaceQosUnset && hostNetworkQosUnset) {
            return true;
        }

        if (ifaceQosUnset != hostNetworkQosUnset) {
            return false;
        }

        return isOutAverageLinkShareInSync() && isOutAverageUpperLimitInSync() && isOutAverageRealTimeInSync();
    }

    private boolean qosUnset(HostNetworkQos ifaceQos) {
        return ifaceQos == null || ifaceQos.isEmpty();
    }

    private boolean isOutAverageRealTimeInSync() {
        return ObjectUtils.objectsEqual(getOutAverageRealtime(ifaceQos), getOutAverageRealtime(hostNetworkQos));
    }

    private boolean isOutAverageUpperLimitInSync() {
        return ObjectUtils.objectsEqual(getOutAverageUpperlimit(ifaceQos), getOutAverageUpperlimit(hostNetworkQos));
    }

    private boolean isOutAverageLinkShareInSync() {
        return ObjectUtils.objectsEqual(getOutAverageLinkshare(ifaceQos), getOutAverageLinkshare(hostNetworkQos));
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

    private boolean isPrimaryAddressExist() {
        return networkDataCenterIpConfigurationDefinition != null
                && networkDataCenterIpConfigurationDefinition.hasPrimaryAddressSet();
    }

    private IPv4Address getPrimaryAddress() {
        return networkDataCenterIpConfigurationDefinition.getPrimaryAddress();
    }

    private void addReportedIpConfiguration(ReportedConfigurations result) {
        if (!isPrimaryAddressExist()) {
            return;
        }
        NetworkBootProtocol definedBootProtocol =
                isPrimaryAddressExist() ? getPrimaryAddress().getBootProtocol() : null;
        result.add(ReportedConfigurationType.BOOT_PROTOCOL, iface.getBootProtocol(), definedBootProtocol);

        if (definedBootProtocol == NetworkBootProtocol.STATIC_IP && iface.getBootProtocol() == definedBootProtocol) {
            result.add(ReportedConfigurationType.NETMASK,
                    iface.getSubnet(),
                    isPrimaryAddressExist() ? getPrimaryAddress().getNetmask() : null);
            result.add(ReportedConfigurationType.IP_ADDRESS,
                    iface.getAddress(),
                    isPrimaryAddressExist() ? getPrimaryAddress().getAddress() : null);
        }
    }
}
