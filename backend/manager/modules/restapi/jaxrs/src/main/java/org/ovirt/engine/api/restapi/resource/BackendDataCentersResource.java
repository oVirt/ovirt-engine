package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.DataCenters;
import org.ovirt.engine.api.model.Versions;
import org.ovirt.engine.api.resource.DataCenterResource;
import org.ovirt.engine.api.resource.DataCentersResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDataCentersResource extends
        AbstractBackendCollectionResource<DataCenter, StoragePool> implements DataCentersResource {

    public BackendDataCentersResource() {
        super(DataCenter.class, StoragePool.class);
    }

    @Override
    public DataCenters list() {
        if (isFiltered()) {
            return mapCollection(getBackendCollection(QueryType.GetAllStoragePools,
                    new QueryParametersBase()));
        } else {
            return mapCollection(getBackendCollection(SearchType.StoragePool));
        }
    }

    @Override
    public DataCenterResource getDataCenterResource(String id) {
        return inject(new BackendDataCenterResource(id, this));
    }

    @Override
    public Response add(DataCenter dataCenter) {
        validateParameters(dataCenter, "name");
        if(!dataCenter.isSetLocal()) {
            validateParameters(dataCenter, "local");
        }
        StoragePool entity = map(dataCenter);
        return performCreate(ActionType.AddEmptyStoragePool,
                               new StoragePoolManagementParameter(entity),
                               new QueryIdResolver<Guid>(QueryType.GetStoragePoolById, IdQueryParameters.class));
    }

    private DataCenters mapCollection(List<StoragePool> entities) {
        DataCenters collection = new DataCenters();
        for (StoragePool entity : entities) {
            collection.getDataCenters().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected DataCenter deprecatedPopulate(DataCenter model, StoragePool entity) {
        IdQueryParameters parameters = new IdQueryParameters(asGuid(model.getId()));
        model.setSupportedVersions(getMapper(List.class,
                                             Versions.class).map(getEntity(List.class,
                                                                                    QueryType.GetAvailableStoragePoolVersions,
                                                                                    parameters,
                                                                                    model.getId()),
                                                                          null));
        return model;
    }

}
