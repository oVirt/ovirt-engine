package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.NonOperationalReason;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
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
        UpdateNetworkToDb(getVds());

        ProceedProxyReturnValue();
    }

    // function return true when vds networks not match it's cluster networks
    // in this case we need to set the vds to non-operational
    public static boolean UpdateNetworkToDb(VDS vds) {
        boolean returnValue = false;
        List<VdsNetworkInterface> dbIfaces = DbFacade.getInstance().getInterfaceDAO().getAllInterfacesForVds(vds.getId());
        List<String> updatedIfaces = new ArrayList<String>();

        // First we check what interfaces need to update/delete
        for (VdsNetworkInterface dbIface : dbIfaces) {
            boolean found = false;

            for (VdsNetworkInterface vdsIface : vds.getInterfaces()) {
                if (dbIface.getName().equals(vdsIface.getName())) {
                    // we preserve only the ID from the Database
                    // everything else is what we got from getVdsCapabilities
                    vdsIface.setId(dbIface.getId());
                    DbFacade.getInstance().getInterfaceDAO().updateInterfaceForVds(vdsIface);
                    updatedIfaces.add(vdsIface.getName());
                    found = true;
                    break;
                }
            }
            if (!found) {
                DbFacade.getInstance().getInterfaceDAO().removeInterfaceFromVds(dbIface.getId());
                DbFacade.getInstance().getInterfaceDAO().removeStatisticsForVds(dbIface.getId());
            }
        }

        // now all that left is add the interfaces that not exists in the Database
        for (VdsNetworkInterface vdsIface : vds.getInterfaces()) {
            if (!updatedIfaces.contains(vdsIface.getName())) {
                DbFacade.getInstance().getInterfaceDAO().saveInterfaceForVds(vdsIface);
                DbFacade.getInstance().getInterfaceDAO().saveStatisticsForVds(vdsIface.getStatistics());
            }
        }

        // here we check if the vds networks match it's cluster networks
        if (vds.getstatus() != VDSStatus.Maintenance && hostIsMissingClusterNetworks(vds)) {
            setNonOperationl(vds, NonOperationalReason.NETWORK_UNREACHABLE);
            returnValue = true;
        }

        return returnValue;
    }

    private static void setNonOperationl(VDS vds, NonOperationalReason reason) {
        ResourceManager.getInstance()
                .getEventListener()
                .vdsNonOperational(vds.getId(),
                        reason,
                        true,
                        true,
                        Guid.Empty);
    }

    private static boolean hostIsMissingClusterNetworks(VDS vds) {
        boolean returnValue = false;
        boolean hasChanges = false;
        List<network> clusterNetworks = DbFacade.getInstance().getNetworkDAO()
                .getAllForCluster(vds.getvds_group_id());
        network tempNetwork;
        java.util.List<network> networks = vds.getNetworks();
        for (network net : clusterNetworks) {
            // LINQ 29456
            // if (vds.Networks.FirstOrDefault(n => n.name == net.name) == null)
            tempNetwork = null;
            String outterNetworkName = net.getname();
            if (outterNetworkName != null) {
                for (network tempNet : networks) {
                    if (outterNetworkName.equals(tempNet.getname())) {
                        tempNetwork = tempNet;
                        break;
                    }
                }
            }
            // LINQ 29456
            if (net.getStatus() == NetworkStatus.Operational && tempNetwork == null) {
                hasChanges = true;
                break;
            }

        }
        if (hasChanges && vds.getstatus() != VDSStatus.Maintenance) {
            ResourceManager.getInstance()
                    .getEventListener()
                    .vdsNonOperational(vds.getId(),
                            NonOperationalReason.NETWORK_UNREACHABLE,
                            true,
                            true,
                            Guid.Empty);
            returnValue = true;
        }
        return returnValue;
    }
}
