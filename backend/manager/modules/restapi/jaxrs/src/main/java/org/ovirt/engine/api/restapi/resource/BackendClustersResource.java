package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendDataCenterResource.getStoragePool;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Clusters;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.resource.ClustersResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClustersResource extends AbstractBackendCollectionResource<Cluster, VDSGroup>
        implements ClustersResource {

    static final String[] SUB_COLLECTIONS = { "networks", "permissions", "glustervolumes", "glusterhooks",
            "affinitygroups", "cpuprofiles" };
    static final String[] VIRT_ONLY_MODE_COLLECTIONS_TO_HIDE = {"glustervolumes", "glusterhooks" };
    public BackendClustersResource() {
        super(Cluster.class, VDSGroup.class, SUB_COLLECTIONS);
    }

    @Override
    public Clusters list() {
        ApplicationMode appMode = getCurrent().get(ApplicationMode.class);

        if (appMode == ApplicationMode.VirtOnly) {
            return listVirtOnly();
        }
        else {
            return listAll();
        }
    }

    private Clusters listVirtOnly() {
        if (isFiltered()) {
            return mapVirtOnlyCollection(getBackendCollection(VdcQueryType.GetAllVdsGroups,
                    new VdcQueryParametersBase()));
        }
        else {
            return mapVirtOnlyCollection(getBackendCollection(SearchType.Cluster));
        }
    }

    private Clusters listAll() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(VdcQueryType.GetAllVdsGroups,
                    new VdcQueryParametersBase()));
        }
        else {
            return mapCollection(getBackendCollection(SearchType.Cluster));
        }
    }

    @Override
    @SingleEntityResource
    public ClusterResource getClusterSubResource(String id) {
        return inject(new BackendClusterResource(id));
    }

    @Override
    public Response add(Cluster cluster) {
        validateParameters(cluster, "name", "dataCenter.name|id");
        validateEnums(Cluster.class, cluster);
        StoragePool pool = getStoragePool(cluster.getDataCenter(), this);
        VDSGroup entity = map(cluster, map(pool));

        if (!cluster.isSetErrorHandling() || !cluster.getErrorHandling().isSetOnError()) {
            entity.setMigrateOnError(null);
        }

        return performCreate(VdcActionType.AddVdsGroup,
                new VdsGroupOperationParameters(entity),
                new QueryIdResolver<Guid>(VdcQueryType.GetVdsGroupById, IdQueryParameters.class));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveVdsGroup, new VdsGroupParametersBase(asGuid(id)));
    }

    protected Clusters mapCollection(List<VDSGroup> entities) {
        Clusters collection = new Clusters();
        for (org.ovirt.engine.core.common.businessentities.VDSGroup entity : entities) {
            collection.getClusters().add(addLinks(map(entity)));
        }
        return collection;
    }

    private Clusters mapVirtOnlyCollection(List<VDSGroup> entities) {
        Clusters collection = new Clusters();
        for (org.ovirt.engine.core.common.businessentities.VDSGroup entity : entities) {
            collection.getClusters().add(addLinks(map(entity), VIRT_ONLY_MODE_COLLECTIONS_TO_HIDE));
        }
        return collection;
    }

    /**
     * Map the storage pool (i.e. datacenter entity) to a VDSGroup instance
     * with the same compatibility version
     */
    protected VDSGroup map(StoragePool pool) {
        return getMapper(StoragePool.class, VDSGroup.class).map(pool, null);
    }

    @Override
    protected Cluster doPopulate(Cluster model, VDSGroup entity) {
        return model;
    }
}
