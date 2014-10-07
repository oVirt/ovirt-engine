package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.ovirt.engine.core.common.businessentities.network.NetworkStatus.OPERATIONAL;

import java.util.ArrayList;
import java.util.Collection;
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
import org.ovirt.engine.core.dao.network.InterfaceDao;
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
                                                                   Map<String, VdsNetworkInterface> nicsByName) {
        List<VdsNetworkInterface> dbIfaces =
                DbFacade.getInstance().getInterfaceDao().getAllInterfacesForVds(host.getId());
        persistTopology(host.getInterfaces(), dbIfaces, nicsByName);

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

            final Map<String, Network> clusterNetworksByName = Entities.entitiesByName(clusterNetworks);
            final Collection<Network> dbHostNetworks = findNetworksOnInterfaces(dbIfaces, clusterNetworksByName);
            logChangedDisplayNetwork(host, dbHostNetworks, dbIfaces);
            logUnsynchronizedNetworks(host, clusterNetworksByName);
        }

        return NonOperationalReason.NONE;
    }

    @Override
    public NonOperationalReason persistAndEnforceNetworkCompliance(VDS host) {
        return persistAndEnforceNetworkCompliance(host, false, null);
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

    private void persistTopology(List<VdsNetworkInterface> reportedNics,
                                 List<VdsNetworkInterface> dbNics,
                                 Map<String, VdsNetworkInterface> nicsByName) {
        InterfaceDao interfaceDAO = DbFacade.getInstance().getInterfaceDao();
        List<String> updatedIfaces = new ArrayList<>();
        List<VdsNetworkInterface> dbIfacesToBatch = new ArrayList<>();
        Map<String, VdsNetworkInterface> hostNicsByNames = Entities.entitiesByName(reportedNics);

        // First we check what interfaces need to update/delete
        for (VdsNetworkInterface dbIface : dbNics) {
            if (hostNicsByNames.containsKey(dbIface.getName())) {
                VdsNetworkInterface vdsIface = hostNicsByNames.get(dbIface.getName());

                // we preserve only the ID and the labels from the Database
                // everything else is what we got from getVdsCapabilities
                vdsIface.setId(dbIface.getId());
                vdsIface.setLabels(dbIface.getLabels());
                vdsIface.setQosOverridden(dbIface.isQosOverridden());
                vdsIface.setCustomProperties(dbIface.getCustomProperties());
                dbIfacesToBatch.add(vdsIface);
                updatedIfaces.add(vdsIface.getName());
            } else {
                interfaceDAO.removeInterfaceFromVds(dbIface.getId());
                interfaceDAO.removeStatisticsForVds(dbIface.getId());
            }
        }

        if (nicsByName != null) {
            updateInterfacesWithUserConfiguration(dbIfacesToBatch, nicsByName);
            updateInterfacesWithUserConfiguration(reportedNics, nicsByName);
        }

        if (!dbIfacesToBatch.isEmpty()) {
            interfaceDAO.massUpdateInterfacesForVds(dbIfacesToBatch);
        }

        // now all that left is add the interfaces that not exists in the Database
        for (VdsNetworkInterface vdsIface : reportedNics) {
            if (!updatedIfaces.contains(vdsIface.getName())) {
                interfaceDAO.saveInterfaceForVds(vdsIface);
                interfaceDAO.saveStatisticsForVds(vdsIface.getStatistics());
            }
        }
    }

    private void updateInterfacesWithUserConfiguration(List<VdsNetworkInterface> nicsForUpdate,
                                                       Map<String, VdsNetworkInterface> nicsByName) {
        for (VdsNetworkInterface nicForUpdate : nicsForUpdate) {
            if (nicsByName.containsKey(nicForUpdate.getName())) {
                VdsNetworkInterface nic = nicsByName.get(nicForUpdate.getName());
                nicForUpdate.setLabels(nic.getLabels());
                nicForUpdate.setQosOverridden(nic.isQosOverridden());
                nicForUpdate.setCustomProperties(nic.getCustomProperties());
            }
        }
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
