package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.ovirt.engine.core.common.businessentities.network.NetworkStatus.OPERATIONAL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface.NetworkImplementationDetails;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.network.NetworkQoSDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.vdsbroker.ResourceManager;
import org.ovirt.engine.core.vdsbroker.vdsbroker.predicates.DisplayInterfaceEqualityPredicate;
import org.ovirt.engine.core.vdsbroker.vdsbroker.predicates.IsNetworkOnInterfacePredicate;

public final class HostNetworkTopologyPersisterImpl implements HostNetworkTopologyPersister {

    private static final HostNetworkTopologyPersister instance = new HostNetworkTopologyPersisterImpl();

    // TODO: replace with CDI + make the class package protected
    public static HostNetworkTopologyPersister getInstance() {
        return instance;
    }

    // Don't new me - use getInstance method
    private HostNetworkTopologyPersisterImpl() {
    }

    @Override
    public NonOperationalReason persistAndEnforceNetworkCompliance(VDS host,
                                                                   boolean skipManagementNetwork,
                                                                   List<VdsNetworkInterface> userConfiguredNics) {
        List<VdsNetworkInterface> dbIfaces =
                DbFacade.getInstance().getInterfaceDao().getAllInterfacesForVds(host.getId());
        persistTopology(host.getInterfaces(), dbIfaces, userConfiguredNics);
        return enforceNetworkCompliance(host, skipManagementNetwork, dbIfaces);
    }

    private NonOperationalReason enforceNetworkCompliance(VDS host,
                                                          boolean skipManagementNetwork,
                                                          List<VdsNetworkInterface> dbIfaces) {
        if (host.getStatus() != VDSStatus.Maintenance) {

            List<Network> clusterNetworks = DbFacade.getInstance().getNetworkDao()
                    .getAllForCluster(host.getVdsGroupId());
            if (skipManagementNetwork) {
                skipManagementNetworkCheck(host.getInterfaces(), clusterNetworks);
            }

            Map<String, String> customLogValues;

            // here we check if the host networks match it's cluster networks
            String networks = getMissingOperationalClusterNetworks(host, clusterNetworks);
            if (networks.length() > 0) {
                customLogValues = new HashMap<String, String>();
                customLogValues.put("Networks", networks);

                setNonOperational(host, NonOperationalReason.NETWORK_UNREACHABLE, customLogValues);
                return NonOperationalReason.NETWORK_UNREACHABLE;
            }

            // Check that VM networks are implemented above a bridge.
            networks = getVmNetworksImplementedAsBridgeless(host, clusterNetworks);
            if (networks.length() > 0) {
                customLogValues = new HashMap<String, String>();
                customLogValues.put("Networks", networks);

                setNonOperational(host, NonOperationalReason.VM_NETWORK_IS_BRIDGELESS, customLogValues);
                return NonOperationalReason.VM_NETWORK_IS_BRIDGELESS;
            }

            auditNetworkCompliance(host, dbIfaces, clusterNetworks);
        }

        return NonOperationalReason.NONE;
    }

    private void auditNetworkCompliance(VDS host,
            List<VdsNetworkInterface> dbIfaces,
            List<Network> clusterNetworks) {
        final Map<String, Network> clusterNetworksByName = Entities.entitiesByName(clusterNetworks);
        final Collection<Network> dbHostNetworks = findNetworksOnInterfaces(dbIfaces, clusterNetworksByName);
        logChangedDisplayNetwork(host, dbHostNetworks, dbIfaces);
        logUnsynchronizedNetworks(host, clusterNetworksByName);
    }

    @Override
    public NonOperationalReason persistAndEnforceNetworkCompliance(VDS host) {
        return persistAndEnforceNetworkCompliance(host, false, Collections.<VdsNetworkInterface> emptyList());
    }

    private void skipManagementNetworkCheck(List<VdsNetworkInterface> ifaces, List<Network> clusterNetworks) {
        String managementNetworkName = NetworkUtils.getEngineNetwork();
        for (VdsNetworkInterface iface : ifaces) {
            if (managementNetworkName.equals(iface.getNetworkName())) {
                return;
            }
        }

        for (Iterator<Network> iterator = clusterNetworks.iterator(); iterator.hasNext();) {
            Network network = iterator.next();
            if (managementNetworkName.equals(network.getName())) {
                iterator.remove();
                break;
            }
        }
    }

    private void logChangedDisplayNetwork(VDS host,
                                          Collection<Network> engineHostNetworks,
                                          Collection<VdsNetworkInterface> engineInterfaces) {

        if (isVmRunningOnHost(host.getId())) {
            final Network engineDisplayNetwork = findDisplayNetwork(engineHostNetworks);

            if (engineDisplayNetwork == null) {
                return;
            }

            final IsNetworkOnInterfacePredicate isNetworkOnInterfacePredicate =
                    new IsNetworkOnInterfacePredicate(engineDisplayNetwork.getName());
            final VdsNetworkInterface vdsmDisplayInterface = LinqUtils.firstOrNull(
                    host.getInterfaces(),
                    isNetworkOnInterfacePredicate);
            final VdsNetworkInterface engineDisplayInterface = LinqUtils.firstOrNull(
                    engineInterfaces,
                    isNetworkOnInterfacePredicate);
            final DisplayInterfaceEqualityPredicate displayIneterfaceEqualityPredicate =
                    new DisplayInterfaceEqualityPredicate(engineDisplayInterface);
            if (vdsmDisplayInterface == null // the display interface is't on host anymore
                || !displayIneterfaceEqualityPredicate.eval(vdsmDisplayInterface)) {
                final AuditLogableBase loggable = new AuditLogableBase(host.getId());
                AuditLogDirector.log(loggable, AuditLogType.NETWORK_UPDATE_DISPLAY_FOR_HOST_WITH_ACTIVE_VM);
            }
        }
    }

    private boolean isVmRunningOnHost(Guid hostId) {
        return !DbFacade.getInstance().getVmDynamicDao().getAllRunningForVds(hostId).isEmpty();
    }

    private Collection<Network> findNetworksOnInterfaces(Collection<VdsNetworkInterface> ifaces,
                                                         Map<String, Network> clusterNetworksByName) {
        final Collection<Network> networks = new ArrayList<>();
        for (VdsNetworkInterface iface : ifaces) {
            final String interfaceNetworkName = iface.getNetworkName();
            if (clusterNetworksByName.containsKey(interfaceNetworkName)) {
                final Network network = clusterNetworksByName.get(interfaceNetworkName);
                networks.add(network);
            }
        }
        return networks;
    }

    private Network findDisplayNetwork(Collection<Network> networks) {
        Network managementNetwork = null;
        for (Network network : networks) {
            if (network.getCluster().isDisplay()) {
                return network;
            }
            if (NetworkUtils.isManagementNetwork(network)) {
                managementNetwork = network;
            }
        }

        return managementNetwork;
    }

    private void logUnsynchronizedNetworks(VDS host, Map<String, Network> networks) {
        List<String> networkNames = new ArrayList<>();
        NetworkQoSDao qosDao = DbFacade.getInstance().getNetworkQosDao();

        for (VdsNetworkInterface iface : host.getInterfaces()) {
            Network network = networks.get(iface.getNetworkName());
            NetworkImplementationDetails networkImplementationDetails =
                    NetworkUtils.calculateNetworkImplementationDetails(network,
                            network == null ? null : qosDao.get(network.getQosId()),
                            iface);

            if (networkImplementationDetails != null
                && !networkImplementationDetails.isInSync()
                && networkImplementationDetails.isManaged()) {
                networkNames.add(iface.getNetworkName());
            }
        }

        if (!networkNames.isEmpty()) {
            AuditLogableBase logable = new AuditLogableBase(host.getId());
            logable.addCustomValue("Networks", StringUtils.join(networkNames, ","));
            AuditLogDirector.log(logable, AuditLogType.VDS_NETWORKS_OUT_OF_SYNC);
        }
    }

    /**
     * Persists the network topology as reported by vdsm, with respect to the following:
     * <ul>
     * <li>Pre-configured provided interfaces will maintain their settings (labels, properties and QoS)</li>
     * <li>Network attachments will be created for nics without it and on which a known network is configured</li>
     * <li>A nic which existed on db and wasn't reported will be removed with its network attachment</li>
     * </ul>
     *
     * @param reportedNics
     *            network interfaces reported by vdsm
     * @param dbNics
     *            network interfaces from the database prior to vdsm report
     * @param userConfiguredNics
     *            network interfaces that should preserve their configuration
     */
    private void persistTopology(List<VdsNetworkInterface> reportedNics,
            List<VdsNetworkInterface> dbNics,
            List<VdsNetworkInterface> userConfiguredNics) {

        final HostNetworkInterfacesPersister networkInterfacesPersister =
                new HostNetworkInterfacesPersisterImpl(DbFacade.getInstance().getInterfaceDao(),
                        reportedNics,
                        dbNics,
                        userConfiguredNics);

        networkInterfacesPersister.persistTopology();
    }

    private String getVmNetworksImplementedAsBridgeless(VDS host, List<Network> clusterNetworks) {
        Map<String, VdsNetworkInterface> interfacesByNetworkName =
                Entities.hostInterfacesByNetworkName(host.getInterfaces());
        List<String> networkNames = new ArrayList<>();

        for (Network net : clusterNetworks) {
            if (net.isVmNetwork()
                && interfacesByNetworkName.containsKey(net.getName())
                && !interfacesByNetworkName.get(net.getName()).isBridged()) {
                networkNames.add(net.getName());
            }
        }

        return StringUtils.join(networkNames, ",");
    }

    private String getMissingOperationalClusterNetworks(VDS host, List<Network> clusterNetworks) {
        Map<String, Network> hostNetworksByName = Entities.entitiesByName(host.getNetworks());
        List<String> networkNames = new ArrayList<>();

        for (Network net : clusterNetworks) {
            if (net.getCluster().getStatus() == OPERATIONAL &&
                net.getCluster().isRequired() &&
                !hostNetworksByName.containsKey(net.getName())) {
                networkNames.add(net.getName());
            }
        }
        return StringUtils.join(networkNames, ",");
    }

    private void setNonOperational(VDS host, NonOperationalReason reason, Map<String, String> customLogValues) {
        ResourceManager.getInstance()
                .getEventListener()
                .vdsNonOperational(host.getId(), reason, true, Guid.Empty, customLogValues);
    }
}
