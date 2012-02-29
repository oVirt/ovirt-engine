package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.ovirt.engine.core.common.businessentities.NetworkStatus.Operational;
import static org.ovirt.engine.core.common.businessentities.NonOperationalReason.VM_NETWORK_IS_BRIDGELESS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.InterfaceDAO;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class CollectVdsNetworkDataVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase>
        extends GetCapabilitiesVDSCommand<P> {
    public CollectVdsNetworkDataVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        // call getVdsCapabilities verb
        super.ExecuteVdsBrokerCommand();

        // update to db
        persistAndEnforceNetworkCompliance(getVds());

        ProceedProxyReturnValue();
    }

    /**
     * Persist this VDS network topology to DB. Set this host to non-operational in case networks doesn't comply with
     * cluster rules:
     * <ul>
     * <li>All mandatory networks(optional=false) should be implemented by the host.
     * <li>All VM networks must be implemented with bridges.
     *
     * @param vds
     * @return
     */
    public static boolean persistAndEnforceNetworkCompliance(VDS vds) {
        persistTopology(vds);

        if (vds.getstatus() != VDSStatus.Maintenance) {

            List<network> clusterNetworks = DbFacade.getInstance().getNetworkDAO()
                    .getAllForCluster(vds.getvds_group_id());
            Map<String, String> customLogValues;

            // here we check if the vds networks match it's cluster networks
            String networks = getMissingOperationalClusterNetworks(vds, clusterNetworks);
            if (networks.length() > 0) {
                customLogValues = new HashMap<String, String>();
                customLogValues.put("Networks", networks);

                setNonOperationl(vds, NonOperationalReason.NETWORK_UNREACHABLE, customLogValues);
                return true;
            }

            // Check that VM networks are implemented above a bridge.
            networks = getVmNetworksImplementedAsBridgeless(vds, clusterNetworks);
            if (networks.length() > 0) {
                customLogValues = new HashMap<String, String>();
                customLogValues.put("Networks", networks);

                setNonOperationl(vds, VM_NETWORK_IS_BRIDGELESS, customLogValues);
                return true;
            }
        }
        return false;
    }

    private static void persistTopology(VDS vds) {
        InterfaceDAO interfaceDAO = DbFacade.getInstance().getInterfaceDAO();
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

    private static String getVmNetworksImplementedAsBridgeless(VDS vds,
            List<network> clusterNetworks) {
        StringBuffer sb = new StringBuffer();
        Map<String, VdsNetworkInterface> interfacesByNetworkName = Entities.interfacesByNetworkName(vds.getInterfaces());

        for (network net : clusterNetworks) {
            if (net.isVmNetwork() &&
                    interfacesByNetworkName.containsKey(net.getName()) &&
                    !interfacesByNetworkName.get(net.getName()).isBridged()) {
                sb.append(net.getname()).append(",");
            }
        }
        return sb.toString();
    }

    private static String getMissingOperationalClusterNetworks(VDS vds, List<network> clusterNetworks) {
        StringBuffer sb = new StringBuffer();
        Map<String, network> vdsNetworksByName = Entities.entitiesByName(vds.getNetworks());

        for (network net : clusterNetworks) {
            if (net.getStatus() == Operational && !vdsNetworksByName.containsKey(net.getName())) {
                sb.append(net.getName()).append(",");
            }
        }
        return sb.toString();
    }

    private static void setNonOperationl(VDS vds,
            NonOperationalReason reason,
            Map<String, String> customLogValues) {
        ResourceManager.getInstance()
                .getEventListener()
                .vdsNonOperational(vds.getId(),
                        reason,
                        true,
                        true,
                        Guid.Empty, customLogValues);
    }
}
