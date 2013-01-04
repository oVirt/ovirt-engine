package org.ovirt.engine.core.bll.network.cluster;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.network.NetworkStatus;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

/**
 * Class to hold common static methods that are used in several different places.
 */
public class NetworkClusterHelper {

    /**
     * Set the network on cluster status in the DB to {@link NetworkStatus#OPERATIONAL} or
     * {@link NetworkStatus#NON_OPERATIONAL} depending on the network state in the cluster (if it's implemented by all
     * the hosts or not) and if it's required or not.
     *
     * @param vdsGroupId
     *            The cluster ID where the network is attached.
     * @param net
     *            The network itself.
     */
    @SuppressWarnings("unchecked")
    public static void setStatus(Guid vdsGroupId, final Network net) {
        NetworkStatus status = NetworkStatus.OPERATIONAL;
        NetworkCluster networkCluster =
                DbFacade.getInstance().getNetworkClusterDao().get(new NetworkClusterId(vdsGroupId, net.getId()));

        if (networkCluster != null) {
            VDSGroup vdsGroup = DbFacade.getInstance().getVdsGroupDao().get(vdsGroupId);

            // Search all vds in cluster that have the specify network, if not the
            // network is not active
            SearchParameters searchParams = new SearchParameters("hosts: cluster = "
                    + vdsGroup.getname(), SearchType.VDS);
            searchParams.setMaxCount(Integer.MAX_VALUE);
            List<VDS> vdsList = (List<VDS>) Backend.getInstance()
                    .runInternalQuery(VdcQueryType.Search, searchParams).getReturnValue();

            if (networkCluster.isRequired()) {
                for (VDS vds : vdsList) {
                    if (vds.getstatus() != VDSStatus.Up) {
                        continue;
                    }
                    List<VdsNetworkInterface> interfaces = (List<VdsNetworkInterface>) Backend
                            .getInstance()
                            .runInternalQuery(VdcQueryType.GetVdsInterfacesByVdsId,
                                    new IdQueryParameters(vds.getId())).getReturnValue();
                    VdsNetworkInterface iface = LinqUtils.firstOrNull(interfaces, new Predicate<VdsNetworkInterface>() {
                        @Override
                        public boolean eval(VdsNetworkInterface i) {
                            return StringUtils.equals(i.getNetworkName(), net.getName());
                        }
                    });
                    if (iface == null) {
                        status = NetworkStatus.NON_OPERATIONAL;
                        break;
                    }
                }
            }

            networkCluster.setStatus(status);
            DbFacade.getInstance().getNetworkClusterDao().updateStatus(networkCluster);
        }
    }

}
