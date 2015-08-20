package org.ovirt.engine.core.utils;

import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.OUT_AVERAGE_LINK_SHARE;
import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.OUT_AVERAGE_REAL_TIME;
import static org.ovirt.engine.core.common.businessentities.network.ReportedConfigurationType.OUT_AVERAGE_UPPER_LIMIT;

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
    private final HostNetworkQos hostNetworkQos;

    public NetworkInSyncWithVdsNetworkInterface(VdsNetworkInterface iface, Network network, HostNetworkQos hostNetworkQos) {
        this.iface = iface;
        this.network = network;
        this.hostNetworkQos = hostNetworkQos;

        ifaceQos = iface.getQos();
    }

    public NetworkInSyncWithVdsNetworkInterface(VdsNetworkInterface iface,
            Network network,
            HostNetworkQos ifaceQos,
            HostNetworkQos hostNetworkQos) {
        this.iface = iface;
        this.network = network;
        this.ifaceQos = ifaceQos;
        this.hostNetworkQos = hostNetworkQos;
    }

    public boolean isNetworkInSync() {
        return isNetworkMtuInSync()
                && isNetworkVlanIdInSync()
                && isNetworkBridgedFlagInSync()
                && qosParametersEqual();
    }

    public ReportedConfigurations reportConfigurationsOnHost () {
        ReportedConfigurations result = new ReportedConfigurations();

        result.setNetworkInSync(isNetworkInSync());

        Integer networkMtu = network.getMtu() == 0 ? NetworkUtils.getDefaultMtu() : network.getMtu();
        result.add(ReportedConfigurationType.MTU, iface.getMtu(), networkMtu, isNetworkMtuInSync());
        result.add(ReportedConfigurationType.BRIDGED, iface.isBridged(), network.isVmNetwork(), isNetworkBridgedFlagInSync());
        result.add(ReportedConfigurationType.VLAN, iface.getVlanId(), network.getVlanId(), isNetworkVlanIdInSync());

        boolean reportHostQos = ifaceQos != null || hostNetworkQos != null;
        if (reportHostQos) {
            result.add(OUT_AVERAGE_LINK_SHARE, getOutAverageLinkshare(ifaceQos), getOutAverageLinkshare(hostNetworkQos), isOutAverageLinkShareInSync());
            result.add(OUT_AVERAGE_UPPER_LIMIT, getOutAverageUpperlimit(ifaceQos), getOutAverageUpperlimit(hostNetworkQos), isOutAverageUpperLimitInSync());
            result.add(OUT_AVERAGE_REAL_TIME, getOutAverageRealtime(ifaceQos), getOutAverageRealtime(hostNetworkQos), isOutAverageRealTimeInSync());
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

}
