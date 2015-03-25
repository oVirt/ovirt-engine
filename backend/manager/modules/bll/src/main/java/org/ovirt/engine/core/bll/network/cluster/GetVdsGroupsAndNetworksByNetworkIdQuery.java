package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;

/**
 * A query to retrieve all VDSGroup-NetworkCluster pairs in the Storage Pool of the given Network. In case the Network
 * is not assigned to a VDSGroup, the VDSGroup is paired with null.
 */
public class GetVdsGroupsAndNetworksByNetworkIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    public GetVdsGroupsAndNetworksByNetworkIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<PairQueryable<VDSGroup, NetworkCluster>> networkClusterPairs = new ArrayList<>();

        Network network = getDbFacade().getNetworkDao().get(getParameters().getId());
        if (network != null && network.getDataCenterId() != null) {
            List<VDSGroup> vdsGroups = getDbFacade().getVdsGroupDao()
                    .getAllForStoragePool(network.getDataCenterId());
            List<NetworkCluster> networkClusters = getDbFacade().getNetworkClusterDao()
                    .getAllForNetwork(getParameters().getId());

            final Map<NetworkClusterId, NetworkCluster> networkClustersById =
                    Entities.businessEntitiesById(networkClusters);

            for (VDSGroup vdsGroup : vdsGroups) {
                networkClusterPairs.add(new PairQueryable<>(vdsGroup,
                        networkClustersById.get(new NetworkClusterId(vdsGroup.getId(), getParameters().getId()))));
            }
        }

        getQueryReturnValue().setReturnValue(networkClusterPairs);
    }
}
