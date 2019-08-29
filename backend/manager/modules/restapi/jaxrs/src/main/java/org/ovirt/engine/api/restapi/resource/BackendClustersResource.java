package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendDataCenterResource.getStoragePool;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Clusters;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.resource.ClustersResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClustersResource extends AbstractBackendCollectionResource<org.ovirt.engine.api.model.Cluster, Cluster>
        implements ClustersResource {

    static final String[] VIRT_ONLY_MODE_COLLECTIONS_TO_HIDE = {"glustervolumes", "glusterhooks" };

    private final ManagementNetworkFinder managementNetworkFinder;

    public BackendClustersResource() {
        super(org.ovirt.engine.api.model.Cluster.class, Cluster.class);
        managementNetworkFinder = new ManagementNetworkFinder(this);
    }

    @Override
    public Clusters list() {
        ApplicationMode appMode = getCurrent().getApplicationMode();

        if (appMode == ApplicationMode.VirtOnly) {
            return listVirtOnly();
        } else {
            return listAll();
        }
    }

    private Clusters listVirtOnly() {
        if (isFiltered()) {
            return mapVirtOnlyCollection(getBackendCollection(QueryType.GetAllClusters,
                    new QueryParametersBase()));
        } else {
            return mapVirtOnlyCollection(getBackendCollection(SearchType.Cluster));
        }
    }

    private Clusters listAll() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(QueryType.GetAllClusters,
                    new QueryParametersBase()));
        } else {
            return mapCollection(getBackendCollection(SearchType.Cluster));
        }
    }

    @Override
    public ClusterResource getClusterResource(String id) {
        return inject(new BackendClusterResource(id, this));
    }

    @Override
    public Response add(org.ovirt.engine.api.model.Cluster cluster) {
        validateParameters(cluster, getMandatoryParameters());
        StoragePool dataCenter = getDataCenter(cluster);
        BackendExternalProviderHelper.completeExternalProviders(this, cluster.getExternalNetworkProviders());
        return performCreate(ActionType.AddCluster,
                createAddCommandParams(cluster, dataCenter),
                new QueryIdResolver<Guid>(QueryType.GetClusterById, IdQueryParameters.class));
    }

    protected String[] getMandatoryParameters() {
        return new String[] { "name", "dataCenter.name|id" };
    }

    protected StoragePool getDataCenter(org.ovirt.engine.api.model.Cluster cluster) {
        return getStoragePool(cluster.getDataCenter(), this);
    }

    private ClusterOperationParameters createAddCommandParams(org.ovirt.engine.api.model.Cluster cluster, StoragePool dataCenter) {
        Cluster clusterEntity = map(cluster, map(dataCenter));

        if (!(cluster.isSetErrorHandling() && cluster.getErrorHandling().isSetOnError())) {
            clusterEntity.setMigrateOnError(null);
        }

        final Guid managementNetworkId = managementNetworkFinder.getManagementNetworkId(cluster, dataCenter.getId());

        return new ClusterOperationParameters(clusterEntity, managementNetworkId);
    }

    protected Clusters mapCollection(List<Cluster> entities) {
        Clusters collection = new Clusters();
        for (Cluster entity : entities) {
            // Specifying LinkHelper.NO_PARENT to explicitly point link to API root:
            //   <cluster href=".../api/clusters/xxx">
            // rather than under datacenter:
            //   <cluster href=".../api/datacenters/yyy/clusters/xxx">
            //
            // (The second option would be selected by default due to the fact that
            // the cluster has a datacenter-id set in it. That is the current
            // LinkHelper behavior)
            collection.getClusters().add(addLinks(populate(map(entity), entity), LinkHelper.NO_PARENT));
        }
        return collection;
    }

    private Clusters mapVirtOnlyCollection(List<Cluster> entities) {
        Clusters collection = new Clusters();
        for (Cluster entity : entities) {
            collection.getClusters().add(addLinks(populate(map(entity), entity), LinkHelper.NO_PARENT, VIRT_ONLY_MODE_COLLECTIONS_TO_HIDE));
        }
        return collection;
    }

    /**
     * Map the storage pool (i.e. datacenter entity) to a Cluster instance
     * with the same compatibility version
     */
    protected Cluster map(StoragePool pool) {
        return getMapper(StoragePool.class, Cluster.class).map(pool, null);
    }

    @Override
    protected org.ovirt.engine.api.model.Cluster doPopulate(org.ovirt.engine.api.model.Cluster cluster, Cluster entity) {
        final Guid clusterId = entity.getId();
        final org.ovirt.engine.core.common.businessentities.network.Network network =
                getOptionalEntity(org.ovirt.engine.core.common.businessentities.network.Network.class,
                        QueryType.GetManagementNetwork,
                        new IdQueryParameters(clusterId),
                        clusterId.toString(),
                        false);
        if (network != null) {
            final Network managementNetwork = new Network();
            managementNetwork.setCluster(cluster);
            managementNetwork.setId(network.getId().toString());
            cluster.setManagementNetwork(managementNetwork);
        }

        return cluster;
    }
}
