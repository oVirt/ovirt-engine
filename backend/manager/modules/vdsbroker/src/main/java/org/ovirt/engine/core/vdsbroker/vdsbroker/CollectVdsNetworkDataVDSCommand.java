package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.ovirt.engine.core.common.businessentities.network.NetworkStatus.OPERATIONAL;

import java.util.ArrayList;
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
import org.ovirt.engine.core.common.vdscommands.CollectVdsNetworkDataVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.NetworkUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class CollectVdsNetworkDataVDSCommand<P extends CollectVdsNetworkDataVDSCommandParameters>
        extends GetCapabilitiesVDSCommand<P> {
    public CollectVdsNetworkDataVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        // call getVdsCapabilities verb
        super.ExecuteVdsBrokerCommand();
        updateNetConfigDirtyFlag();

        // update to db
        persistAndEnforceNetworkCompliance(getVds(), getParameters().isManagementNetworkSkipped());

        ProceedProxyReturnValue();
    }

    /**
     * Update the {@link VdsDynamic#getnet_config_dirty()} field in the DB.<br>
     * The update is done in a new transaction since we don't care if afterwards something goes wrong, but we would like
     * to minimize races with other command that update the {@link VdsDynamic} entity in the DB.
     */
    private void updateNetConfigDirtyFlag() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                DbFacade.getInstance()
                        .getVdsDynamicDao()
                        .updateNetConfigDirty(getVds().getId(), getVds().getNetConfigDirty());
                return null;
            }
        });
    }

    /**
     * Persist this VDS network topology to DB. Set this host to non-operational in case networks doesn't comply with
     * cluster rules:
     * <ul>
     * <li>All mandatory networks(optional=false) should be implemented by the host.
     * <li>All VM networks must be implemented with bridges.
     *
     * @param vds
     * @param skipManagementNetwork
     *            if <code>true</code> skip validations for the management network (existence on the host or configured
     *            properly)
     * @return The reason for non-operability of the host or <code>NonOperationalReason.NONE</code>
     */
    public static NonOperationalReason persistAndEnforceNetworkCompliance(VDS vds, boolean skipManagementNetwork) {
        persistTopology(vds);

        if (vds.getStatus() != VDSStatus.Maintenance) {

            List<Network> clusterNetworks = DbFacade.getInstance().getNetworkDao()
                    .getAllForCluster(vds.getVdsGroupId());
            if (skipManagementNetwork) {
                skipManagementNetworkCheck(vds.getInterfaces(), clusterNetworks);
            }

            Map<String, String> customLogValues;

            // here we check if the vds networks match it's cluster networks
            String networks = getMissingOperationalClusterNetworks(vds, clusterNetworks);
            if (networks.length() > 0) {
                customLogValues = new HashMap<String, String>();
                customLogValues.put("Networks", networks);

                setNonOperationl(vds, NonOperationalReason.NETWORK_UNREACHABLE, customLogValues);
                return NonOperationalReason.NETWORK_UNREACHABLE;
            }

            // Check that VM networks are implemented above a bridge.
            networks = getVmNetworksImplementedAsBridgeless(vds, clusterNetworks);
            if (networks.length() > 0) {
                customLogValues = new HashMap<String, String>();
                customLogValues.put("Networks", networks);

                setNonOperationl(vds, NonOperationalReason.VM_NETWORK_IS_BRIDGELESS, customLogValues);
                return NonOperationalReason.VM_NETWORK_IS_BRIDGELESS;
            }

            logUnsynchronizedNetworks(vds, Entities.entitiesByName(clusterNetworks));
        }
        return NonOperationalReason.NONE;
    }

    private static void skipManagementNetworkCheck(List<VdsNetworkInterface> ifaces, List<Network> clusterNetworks) {
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

    private static void logUnsynchronizedNetworks(VDS vds, Map<String, Network> networks) {
        List<String> networkNames = new ArrayList<String>();

        for (VdsNetworkInterface iface : vds.getInterfaces()) {
            NetworkImplementationDetails networkImplementationDetails =
                    NetworkUtils.calculateNetworkImplementationDetails(networks, iface);

            if (networkImplementationDetails != null && !networkImplementationDetails.isInSync()) {
                networkNames.add(iface.getNetworkName());
            }
        }

        if (!networkNames.isEmpty()) {
            AuditLogableBase logable = new AuditLogableBase(vds.getId());
            logable.addCustomValue("Networks", StringUtils.join(networkNames, ","));
            AuditLogDirector.log(logable, AuditLogType.VDS_NETWORKS_OUT_OF_SYNC);
        }
    }

    private static void persistTopology(VDS vds) {
        InterfaceDao interfaceDAO = DbFacade.getInstance().getInterfaceDao();
        List<VdsNetworkInterface> dbIfaces = interfaceDAO.getAllInterfacesForVds(vds.getId());
        List<String> updatedIfaces = new ArrayList<String>();

        // First we check what interfaces need to update/delete
        for (VdsNetworkInterface dbIface : dbIfaces) {
            boolean found = false;

            for (VdsNetworkInterface vdsIface : vds.getInterfaces()) {
                if (dbIface.getName().equals(vdsIface.getName())) {
                    // we preserve only the ID from the Database
                    // everything else is what we got from getVdsCapabilities
                    vdsIface.setId(dbIface.getId());
                    interfaceDAO.updateInterfaceForVds(vdsIface);
                    updatedIfaces.add(vdsIface.getName());
                    found = true;
                    break;
                }
            }
            if (!found) {
                interfaceDAO.removeInterfaceFromVds(dbIface.getId());
                interfaceDAO.removeStatisticsForVds(dbIface.getId());
            }
        }

        // now all that left is add the interfaces that not exists in the Database
        for (VdsNetworkInterface vdsIface : vds.getInterfaces()) {
            if (!updatedIfaces.contains(vdsIface.getName())) {
                interfaceDAO.saveInterfaceForVds(vdsIface);
                interfaceDAO.saveStatisticsForVds(vdsIface.getStatistics());
            }
        }
    }

    private static String getVmNetworksImplementedAsBridgeless(VDS vds, List<Network> clusterNetworks) {
        Map<String, VdsNetworkInterface> interfacesByNetworkName = Entities.interfacesByNetworkName(vds.getInterfaces());
        List<String> networkNames = new ArrayList<String>();

        for (Network net : clusterNetworks) {
            if (net.isVmNetwork()
                    && interfacesByNetworkName.containsKey(net.getName())
                    && !interfacesByNetworkName.get(net.getName()).isBridged()) {
                networkNames.add(net.getName());
            }
        }

        return StringUtils.join(networkNames, ",");
    }

    private static String getMissingOperationalClusterNetworks(VDS vds, List<Network> clusterNetworks) {
        Map<String, Network> vdsNetworksByName = Entities.entitiesByName(vds.getNetworks());
        List<String> networkNames = new ArrayList<String>();

        for (Network net : clusterNetworks) {
            if (net.getCluster().getStatus() == OPERATIONAL &&
                    net.getCluster().isRequired() &&
                    !vdsNetworksByName.containsKey(net.getName())) {
                networkNames.add(net.getName());
            }
        }
        return StringUtils.join(networkNames, ",");
    }

    private static void setNonOperationl(VDS vds, NonOperationalReason reason, Map<String, String> customLogValues) {
        ResourceManager.getInstance()
                .getEventListener()
                .vdsNonOperational(vds.getId(), reason, true, true, Guid.Empty, customLogValues);
    }
}
