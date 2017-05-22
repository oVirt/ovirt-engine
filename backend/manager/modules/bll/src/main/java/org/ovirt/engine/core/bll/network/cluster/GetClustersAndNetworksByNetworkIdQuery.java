package org.ovirt.engine.core.bll.network.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.QueriesCommandBase;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.businessentities.network.NetworkClusterId;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.network.NetworkClusterDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

/**
 * A query to retrieve all Cluster-NetworkCluster pairs in the Storage Pool of the given Network. In case the Network
 * is not assigned to a Cluster, the Cluster is paired with null.
 */
public class GetClustersAndNetworksByNetworkIdQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private NetworkDao networkDao;

    @Inject
    private ClusterDao clusterDao;

    @Inject
    private NetworkClusterDao networkClusterDao;

    public GetClustersAndNetworksByNetworkIdQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<PairQueryable<Cluster, NetworkCluster>> networkClusterPairs = new ArrayList<>();

        Network network = networkDao.get(getParameters().getId());
        if (network != null && network.getDataCenterId() != null) {
            List<Cluster> clusters = clusterDao.getAllForStoragePool(network.getDataCenterId());
            List<NetworkCluster> networkClusters = networkClusterDao.getAllForNetwork(getParameters().getId());

            final Map<NetworkClusterId, NetworkCluster> networkClustersById =
                    Entities.businessEntitiesById(networkClusters);

            for (Cluster cluster : clusters) {
                networkClusterPairs.add(new PairQueryable<>(cluster,
                        networkClustersById.get(new NetworkClusterId(cluster.getId(), getParameters().getId()))));
            }
        }

        getQueryReturnValue().setReturnValue(networkClusterPairs);
    }
}
