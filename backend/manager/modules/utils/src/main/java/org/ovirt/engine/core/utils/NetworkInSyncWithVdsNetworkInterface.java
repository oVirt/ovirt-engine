package org.ovirt.engine.core.utils;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.network.HostNetworkQos;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType;
import org.ovirt.engine.core.common.businessentities.network.ReportedConfigurations;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.ObjectUtils;

public class NetworkInSyncWithVdsNetworkInterface {

    private final VdsNetworkInterface iface;
    private final Network network;
    private final HostNetworkQos ifaceQos;
    private final HostNetworkQos networkQos;

    public NetworkInSyncWithVdsNetworkInterface(VdsNetworkInterface iface, Network network, HostNetworkQos networkQos) {
        this.iface = iface;
        this.network = network;
        this.networkQos = networkQos;

        ifaceQos = iface.getQos();
    }

    public NetworkInSyncWithVdsNetworkInterface(VdsNetworkInterface iface,
            Network network,
            HostNetworkQos ifaceQos,
            HostNetworkQos networkQos) {
        this.iface = iface;
        this.network = network;
        this.ifaceQos = ifaceQos;
        this.networkQos = networkQos;
    }

    public boolean isNetworkInSync() {
        return isNetworkMtuInSync()
                && isNetworkVlanIdInSync()
                && isNetworkBridgedFlagInSync()
                && (qosParametersEqual() || iface.isQosOverridden());
    }

    public ReportedConfigurations reportConfigurationsOnHost () {
        ReportedConfigurations result = new ReportedConfigurations();

        result.setNetworkInSync(isNetworkInSync());

        result.add(ReportedConfigurationType.MTU, iface.getMtu(), isNetworkMtuInSync());
        result.add(ReportedConfigurationType.BRIDGED, iface.isBridged(), isNetworkBridgedFlagInSync());
        result.add(ReportedConfigurationType.VLAN, iface.getVlanId(), isNetworkVlanIdInSync());

        boolean reportHostQos = ifaceQos != null;
        if (reportHostQos) {
            //TODO MM: lets say, that Qos is overridden, so whole network is 'inSync' while following parameters are 'out of sync'. Can be little bit confusing.
            result.add(ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE, ifaceQos.getOutAverageLinkshare(), isOutAverageLinkShareInSync());
            result.add(ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT, ifaceQos.getOutAverageUpperlimit(), isOutAverageUpperLimitInSync());
            result.add(ReportedConfigurationType.OUT_AVERAGE_REAL_TIME, ifaceQos.getOutAverageRealtime(), isOutAverageRealTimeInSync());
        }

        return result;
    }

    private boolean isNetworkBridgedFlagInSync() {
        return iface.isBridged() == network.isVmNetwork();
    }

    private boolean isNetworkVlanIdInSync() {
        return Objects.equals(iface.getVlanId(), network.getVlanId());
    }

    private boolean isNetworkMtuInSync() {
        boolean networkValueSetToDefaultMtu = network.getMtu() == 0;
        boolean ifaceValueSetToDefaultMtu = iface.getMtu() == NetworkUtils.getDefaultMtu();
        boolean bothUsesDefaultValue = networkValueSetToDefaultMtu && ifaceValueSetToDefaultMtu;
        return (bothUsesDefaultValue || iface.getMtu() == network.getMtu());
    }

    public boolean qosParametersEqual() {
        if (ifaceQos == networkQos) {
            return true;
        }

        if (ifaceQos == null || networkQos == null) {
            return false;
        }

        return isOutAverageLinkShareInSync() && isOutAverageUpperLimitInSync() && isOutAverageRealTimeInSync();
    }

    private boolean isOutAverageRealTimeInSync() {
        return ObjectUtils.objectsEqual(ifaceQos.getOutAverageRealtime(), networkQos.getOutAverageRealtime());
    }

    private boolean isOutAverageUpperLimitInSync() {
        return ObjectUtils.objectsEqual(ifaceQos.getOutAverageUpperlimit(), networkQos.getOutAverageUpperlimit());
    }

    private boolean isOutAverageLinkShareInSync() {
        return ObjectUtils.objectsEqual(ifaceQos.getOutAverageLinkshare(), networkQos.getOutAverageLinkshare());
    }

}
