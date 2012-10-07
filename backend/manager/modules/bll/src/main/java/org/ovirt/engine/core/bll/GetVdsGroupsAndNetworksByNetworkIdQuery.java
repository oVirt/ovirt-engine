package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.NetworkClusterId;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network_cluster;
import org.ovirt.engine.core.common.queries.NetworkIdParameters;
import org.ovirt.engine.core.common.utils.Pair;

/**
 * A query to retrieve all VDSGroup-network_cluster pairs in the Storage Pool of the given Network. In case the Network
 * is not assigned to a VDSGroup, the VDSGroup is paired with null.
 */
public class GetVdsGroupsAndNetworksByNetworkIdQuery<P extends NetworkIdParameters> extends QueriesCommandBase<P> {
    public GetVdsGroupsAndNetworksByNetworkIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<Pair<VDSGroup, network_cluster>> networkClusterPairs = new ArrayList<Pair<VDSGroup, network_cluster>>();

        Network network = getDbFacade().getNetworkDao().get(getParameters().getNetworkId());
        if (network != null && network.getstorage_pool_id() != null) {
            List<VDSGroup> vdsGroups = getDbFacade().getVdsGroupDao()
                    .getAllForStoragePool(network.getstorage_pool_id().getValue());
            List<network_cluster> networkClusters = getDbFacade().getNetworkClusterDao()
                    .getAllForNetwork(getParameters().getNetworkId());

            final Map<NetworkClusterId, network_cluster> networkClustersById =
                    Entities.businessEntitiesById(networkClusters);

            for (VDSGroup vdsGroup : vdsGroups) {
                networkClusterPairs.add(new Pair<VDSGroup, network_cluster>(vdsGroup,
                        networkClustersById.get(new NetworkClusterId(vdsGroup.getId(), getParameters().getNetworkId()))));
            }
        }

        getQueryReturnValue().setReturnValue(networkClusterPairs);
    }
}
