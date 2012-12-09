package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.ActionResource;
import org.ovirt.engine.api.resource.AttachedStorageDomainResource;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendAttachedStorageDomainResource
    extends AbstractBackendActionableResource<StorageDomain, storage_domains>
    implements AttachedStorageDomainResource {

    protected Guid dataCenterId;

    public BackendAttachedStorageDomainResource(String id, Guid dataCenterId) {
        super(id, StorageDomain.class, storage_domains.class);
        this.dataCenterId = dataCenterId;
    }

    @Override
    public StorageDomain get() {
        return performGet(VdcQueryType.GetStorageDomainByIdAndStoragePoolId,
                          new StorageDomainAndPoolQueryParameters(guid, dataCenterId));
    }

    @Override
    public Response activate(Action action) {
        return doAction(VdcActionType.ActivateStorageDomain,
                        new StorageDomainPoolParametersBase(guid, dataCenterId),
                        action);
    }

    @Override
    public Response deactivate(Action action) {
        return doAction(VdcActionType.DeactivateStorageDomain,
                        new StorageDomainPoolParametersBase(guid, dataCenterId),
                        action);
    }

    @Override
    public ActionResource getActionSubresource(String action, String ids) {
        return inject(new BackendActionResource(action, ids));
    }

    @Override
    protected StorageDomain addParents(StorageDomain storageDomain) {
        storageDomain.setDataCenter(new DataCenter());
        storageDomain.getDataCenter().setId(dataCenterId.toString());
        return storageDomain;
    }

    @Override
    protected StorageDomain map(storage_domains entity, StorageDomain template) {
        BackendStorageDomainsResource resource = new BackendStorageDomainsResource();
        inject(resource);
        return resource.map(entity, template);
    }

    @Override
    protected StorageDomain doPopulate(StorageDomain model, storage_domains entity) {
        return model;
    }
}
