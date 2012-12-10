package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.model.StorageType;
import org.ovirt.engine.api.resource.AttachedStorageDomainResource;
import org.ovirt.engine.api.resource.AttachedStorageDomainsResource;

import org.ovirt.engine.core.common.action.DetachStorageDomainFromPoolParameters;
import org.ovirt.engine.core.common.action.RemoveStorageDomainParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;
import org.ovirt.engine.core.common.queries.StoragePoolQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendAttachedStorageDomainsResource
    extends AbstractBackendCollectionResource<StorageDomain, storage_domains>
    implements AttachedStorageDomainsResource {

    protected Guid dataCenterId;

    public BackendAttachedStorageDomainsResource(String dataCenterId) {
        super(StorageDomain.class, storage_domains.class);
        this.dataCenterId = asGuid(dataCenterId);
    }

    @Override
    public StorageDomains list() {
        StorageDomains storageDomains = new StorageDomains();

        for (storage_domains entity : getBackendCollection(storage_domains.class,
                                                           VdcQueryType.GetStorageDomainsByStoragePoolId,
                                                           new StoragePoolQueryParametersBase(dataCenterId))) {
            storageDomains.getStorageDomains().add(addLinks(map(entity)));
        }

        return storageDomains;
    }

    @Override
    @SingleEntityResource
    public AttachedStorageDomainResource getAttachedStorageDomainSubResource(String id) {
        return inject(new BackendAttachedStorageDomainResource(id, dataCenterId));
    }

    @Override
    public Response add(StorageDomain storageDomain) {
        validateParameters(storageDomain, "id|name");

        Guid storageDomainId;
        if (storageDomain.isSetId()) {
            storageDomainId = asGuid(storageDomain.getId());
        } else {
            storageDomainId = lookupStorageDomainIdByName(storageDomain.getName());
        }

        return performCreation(VdcActionType.AttachStorageDomainToPool,
                               new DetachStorageDomainFromPoolParameters(storageDomainId, dataCenterId),
                               new StorageDomainIdResolver(storageDomainId));
    }

    @Override
    public Response performRemove(String id) {
        StorageDomain storageDomain = getAttachedStorageDomainSubResource(id).get();
        if (storageDomain.getStorage().getType().equals(StorageType.LOCALFS.value())) {
            RemoveStorageDomainParameters params = new RemoveStorageDomainParameters(asGuid(id));
            params.setDoFormat(true);
            return (performAction(VdcActionType.RemoveStorageDomain, params));
        } else {
            return (performAction(VdcActionType.DetachStorageDomainFromPool,
                    new DetachStorageDomainFromPoolParameters(asGuid(id), dataCenterId)));
        }
    }

    @Override
    protected StorageDomain addParents(StorageDomain storageDomain) {
        storageDomain.setDataCenter(new DataCenter());
        storageDomain.getDataCenter().setId(dataCenterId.toString());
        return storageDomain;
    }

    protected Guid lookupStorageDomainIdByName(String name) {
        return getEntity(storage_domains.class,
                         SearchType.StorageDomain,
                         "Storage: name=" + name).getId();
    }

    protected storage_domains  lookupStorageDomainById(Guid storageDomainId) {
        return getEntity(storage_domains.class,
                         VdcQueryType.GetStorageDomainByIdAndStoragePoolId,
                         new StorageDomainAndPoolQueryParameters(storageDomainId, dataCenterId),
                         storageDomainId.toString());
    }

    protected class StorageDomainIdResolver extends EntityIdResolver<Guid> {

        private Guid storageDomainId;

        public StorageDomainIdResolver(Guid storageDomainId) {
            this.storageDomainId = storageDomainId;
        }

        @Override
        public storage_domains lookupEntity(Guid nullId) {
            assert(nullId == null); // attach action return nothing, lookup original id instead
            return lookupStorageDomainById(storageDomainId);
        }
    }

    @Override
    protected StorageDomain map(storage_domains entity, StorageDomain template) {
        BackendStorageDomainsResource resource = new BackendStorageDomainsResource();
        inject(resource);
        return resource.map(entity, template);
    }
}
