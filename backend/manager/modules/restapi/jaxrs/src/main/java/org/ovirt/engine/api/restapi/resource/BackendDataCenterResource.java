package org.ovirt.engine.api.restapi.resource;


import java.util.List;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.AttachedStorageDomainsResource;
import org.ovirt.engine.api.resource.DataCenterResource;
import org.ovirt.engine.api.resource.QuotasResource;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.StorageDomainQueryParametersBase;
import org.ovirt.engine.core.common.queries.StoragePoolQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendDataCentersResource.SUB_COLLECTIONS;

public class BackendDataCenterResource extends AbstractBackendSubResource<DataCenter, storage_pool>
        implements DataCenterResource {

    private BackendDataCentersResource parent;

    public BackendDataCenterResource(String id, BackendDataCentersResource parent) {
        super(id, DataCenter.class, storage_pool.class, SUB_COLLECTIONS);
        this.parent = parent;
    }

    @Override
    public DataCenter get() {
        return performGet(VdcQueryType.GetStoragePoolById, new StoragePoolQueryParametersBase(guid));
    }

    @Override
    public DataCenter update(DataCenter incoming) {
        validateEnums(DataCenter.class, incoming);
        return performUpdate(incoming,
                             new QueryIdResolver<Guid>(VdcQueryType.GetStoragePoolById, StoragePoolQueryParametersBase.class),
                             VdcActionType.UpdateStoragePool,
                             new UpdateParametersProvider());
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return inject(new BackendAssignedPermissionsResource(guid,
                                                             VdcQueryType.GetPermissionsForObject,
                                                             new GetPermissionsForObjectParameters(guid),
                                                             DataCenter.class,
                                                             VdcObjectType.StoragePool));
    }

    @Override
    public AttachedStorageDomainsResource getAttachedStorageDomainsResource() {
        return inject(new BackendAttachedStorageDomainsResource(id));
    }

    @Override
    public QuotasResource getQuotasResource() {
         return inject(new BackendQuotasResource(id));
    }

    public BackendDataCentersResource getParent() {
        return parent;
    }

    @Override
    protected DataCenter doPopulate(DataCenter model, storage_pool entity) {
        return parent.doPopulate(model, entity);
    }

    @Override
    protected DataCenter deprecatedPopulate(DataCenter model, storage_pool entity) {
        return parent.deprecatedPopulate(model, entity);
    }

    protected class UpdateParametersProvider implements
            ParametersProvider<DataCenter, storage_pool> {
        @Override
        public VdcActionParametersBase getParameters(DataCenter incoming, storage_pool entity) {
            return new StoragePoolManagementParameter(map(incoming, entity));
        }
    }

    /**
     * Get the storage pool (i.e. datacenter entity) associated with the given
     * cluster.
     */
    @SuppressWarnings("unchecked")
    public static storage_pool getStoragePool(Cluster cluster, AbstractBackendResource parent) {
        storage_pool pool = null;
        if (cluster.getDataCenter().isSetId()) {
            String id = cluster.getDataCenter().getId();
            pool = (storage_pool)parent.getEntity(storage_pool.class, VdcQueryType.GetStoragePoolById,
                    new StoragePoolQueryParametersBase(new Guid(id)), "Datacenter: id=" + id);
        } else {
            pool = (storage_pool)parent.getEntity(storage_pool.class, SearchType.StoragePool, "Datacenter: name="
                    + cluster.getDataCenter().getName());
            cluster.getDataCenter().setId(pool.getId().toString());
        }
        return pool;
    }

    /**
     * Get the storage pools (i.e. datacenter entity) associated with the given
     * storagedomain.
     */
    @SuppressWarnings("unchecked")
    public static  List<storage_pool> getStoragePools(Guid storageDomainId, AbstractBackendResource parent) {
        return (List<storage_pool>)parent.getEntity(List.class,
                                                    VdcQueryType.GetStoragePoolsByStorageDomainId,
                                                    new StorageDomainQueryParametersBase(storageDomainId),
                                                    "Datacenters",
                                                    true);
    }
}
