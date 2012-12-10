package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.DataCenters;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.model.SupportedVersions;
import org.ovirt.engine.api.resource.DataCenterResource;
import org.ovirt.engine.api.resource.DataCentersResource;

import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetAvailableStoragePoolVersionsParameters;
import org.ovirt.engine.core.common.queries.StoragePoolQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDataCentersResource extends
        AbstractBackendCollectionResource<DataCenter, storage_pool> implements DataCentersResource {

    static final String[] SUB_COLLECTIONS = {"storagedomains", "permissions", "quotas"};

    public BackendDataCentersResource() {
        super(DataCenter.class, storage_pool.class, SUB_COLLECTIONS);
    }

    @Override
    public DataCenters list() {
        if (isFiltered())
            return mapCollection(getBackendCollection(VdcQueryType.GetAllStoragePools,
                    new VdcQueryParametersBase()));
        else
            return mapCollection(getBackendCollection(SearchType.StoragePool));
    }

    @Override
    @SingleEntityResource
    public DataCenterResource getDataCenterSubResource(String id) {
        return inject(new BackendDataCenterResource(id, this));
    }

    @Override
    public Response add(DataCenter dataCenter) {
        validateParameters(dataCenter, "name", "storageType");
        validateEnums(DataCenter.class, dataCenter);
        validateEnum(StorageType.class, dataCenter.getStorageType().toUpperCase());
        storage_pool entity = map(dataCenter);
        return performCreation(VdcActionType.AddEmptyStoragePool,
                               new StoragePoolManagementParameter(entity),
                               new QueryIdResolver<Guid>(VdcQueryType.GetStoragePoolById, StoragePoolQueryParametersBase.class));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveStoragePool, new StoragePoolParametersBase(asGuid(id)));
    }

    @Override
    public Response remove(String id, Action action) {
        getEntity(id);
        StoragePoolParametersBase params = new StoragePoolParametersBase(asGuid(id));
        if (action != null && action.isSetForce()) {
            params.setForceDelete(action.isForce());
        }
        return performAction(VdcActionType.RemoveStoragePool, params);
    }

    private DataCenters mapCollection(List<storage_pool> entities) {
        DataCenters collection = new DataCenters();
        for (storage_pool entity : entities) {
            collection.getDataCenters().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected DataCenter populate(DataCenter model, storage_pool entity) {
        GetAvailableStoragePoolVersionsParameters parameters = new GetAvailableStoragePoolVersionsParameters();
        parameters.setStoragePoolId(new Guid(model.getId()));
        model.setSupportedVersions(getMapper(List.class,
                                             SupportedVersions.class).map(getEntity(ArrayList.class,
                                                                                    VdcQueryType.GetAvailableStoragePoolVersions,
                                                                                    parameters,
                                                                                    model.getId()),
                                                                          null));
        return model;
    }
}
