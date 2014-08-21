package org.ovirt.engine.api.restapi.resource;

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
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDataCentersResource extends
        AbstractBackendCollectionResource<DataCenter, StoragePool> implements DataCentersResource {

    static final String[] SUB_COLLECTIONS =
            { "storagedomains", "clusters", "networks", "permissions", "quotas", "iscsibonds" };

    public BackendDataCentersResource() {
        super(DataCenter.class, StoragePool.class, SUB_COLLECTIONS);
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
        validateParameters(dataCenter, "name");
        validateEnums(DataCenter.class, dataCenter);
        if (dataCenter.isSetStorageType()) {
            validateEnum(StorageType.class, dataCenter.getStorageType().toUpperCase());
        }
        else if(!dataCenter.isSetLocal()) {
            validateParameters(dataCenter, "local");
        }
        StoragePool entity = map(dataCenter);
        return performCreate(VdcActionType.AddEmptyStoragePool,
                               new StoragePoolManagementParameter(entity),
                               new QueryIdResolver<Guid>(VdcQueryType.GetStoragePoolById, IdQueryParameters.class));
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

    private DataCenters mapCollection(List<StoragePool> entities) {
        DataCenters collection = new DataCenters();
        for (StoragePool entity : entities) {
            collection.getDataCenters().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected DataCenter doPopulate(DataCenter model, StoragePool entity) {
        return model;
    }

    @Override
    protected DataCenter deprecatedPopulate(DataCenter model, StoragePool entity) {
        IdQueryParameters parameters = new IdQueryParameters(new Guid(model.getId()));
        model.setSupportedVersions(getMapper(List.class,
                                             SupportedVersions.class).map(getEntity(List.class,
                                                                                    VdcQueryType.GetAvailableStoragePoolVersions,
                                                                                    parameters,
                                                                                    model.getId()),
                                                                          null));
        return model;
    }

}
