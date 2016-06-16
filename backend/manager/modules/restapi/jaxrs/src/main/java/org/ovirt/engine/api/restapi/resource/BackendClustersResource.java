package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendDataCenterResource.getStoragePool;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Clusters;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.resource.ClustersResource;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClustersResource extends AbstractBackendCollectionResource<org.ovirt.engine.api.model.Cluster, Cluster>
        implements ClustersResource {

    static final String[] SUB_COLLECTIONS = { "networks", "permissions", "glustervolumes", "glusterhooks",
            "affinitygroups", "cpuprofiles" };
    static final String[] VIRT_ONLY_MODE_COLLECTIONS_TO_HIDE = {"glustervolumes", "glusterhooks" };

    private final ManagementNetworkFinder managementNetworkFinder;

    public BackendClustersResource() {
        super(org.ovirt.engine.api.model.Cluster.class, Cluster.class, SUB_COLLECTIONS);
        managementNetworkFinder = new ManagementNetworkFinder(this);
    }

    @Override
    public Clusters list() {
        ApplicationMode appMode = getCurrent().getApplicationMode();

        if (appMode == ApplicationMode.VirtOnly) {
            return listVirtOnly();
        }
        else {
            return listAll();
        }
    }

    private Clusters listVirtOnly() {
        if (isFiltered()) {
            return mapVirtOnlyCollection(getBackendCollection(VdcQueryType.GetAllClusters,
                    new VdcQueryParametersBase()));
        }
        else {
            return mapVirtOnlyCollection(getBackendCollection(SearchType.Cluster));
        }
    }

    private Clusters listAll() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(VdcQueryType.GetAllClusters,
                    new VdcQueryParametersBase()));
        }
        else {
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
        return performCreate(VdcActionType.AddCluster,
                createAddCommandParams(cluster, dataCenter),
                new QueryIdResolver<Guid>(VdcQueryType.GetClusterById, IdQueryParameters.class));
    }

    protected String[] getMandatoryParameters() {
        return new String[] { "name", "dataCenter.name|id" };
    }

    protected StoragePool getDataCenter(org.ovirt.engine.api.model.Cluster cluster) {
        return getStoragePool(cluster.getDataCenter(), this);
    }

    private ManagementNetworkOnClusterOperationParameters createAddCommandParams(org.ovirt.engine.api.model.Cluster cluster, StoragePool dataCenter) {
        Cluster clusterEntity = map(cluster, map(dataCenter));

        if (!(cluster.isSetErrorHandling() && cluster.getErrorHandling().isSetOnError())) {
            clusterEntity.setMigrateOnError(null);
        }

        final Guid managementNetworkId = managementNetworkFinder.getManagementNetworkId(cluster, dataCenter.getId());

        return new ManagementNetworkOnClusterOperationParameters(clusterEntity, managementNetworkId);
    }

    protected Clusters mapCollection(List<Cluster> entities) {
        Clusters collection = new Clusters();
        for (Cluster entity : entities) {
            collection.getClusters().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    private Clusters mapVirtOnlyCollection(List<Cluster> entities) {
        Clusters collection = new Clusters();
        for (Cluster entity : entities) {
            collection.getClusters().add(addLinks(populate(map(entity), entity), VIRT_ONLY_MODE_COLLECTIONS_TO_HIDE));
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
                        VdcQueryType.GetManagementNetwork,
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
