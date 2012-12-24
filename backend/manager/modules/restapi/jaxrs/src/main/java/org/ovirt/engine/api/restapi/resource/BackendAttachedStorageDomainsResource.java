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

import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;
import org.ovirt.engine.core.common.queries.StoragePoolQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainResource.isIsoDomain;

public class BackendAttachedStorageDomainsResource
    extends AbstractBackendCollectionResource<StorageDomain, org.ovirt.engine.core.common.businessentities.StorageDomain>
    implements AttachedStorageDomainsResource {

    static final String[] SUB_COLLECTIONS = { "disks" };

    protected Guid dataCenterId;

    public BackendAttachedStorageDomainsResource(String dataCenterId) {
        super(StorageDomain.class, org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        this.dataCenterId = asGuid(dataCenterId);
    }

    @Override
    public StorageDomains list() {
        StorageDomains storageDomains = new StorageDomains();

        for (org.ovirt.engine.core.common.businessentities.StorageDomain entity : getBackendCollection(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                                                           VdcQueryType.GetStorageDomainsByStoragePoolId,
                                                           new StoragePoolQueryParametersBase(dataCenterId))) {
            storageDomains.getStorageDomains().add(addLinks(map(entity), getLinksToExclude(entity)));
        }

        return storageDomains;
    }

    @Override
    @SingleEntityResource
    public AttachedStorageDomainResource getAttachedStorageDomainSubResource(String id) {
        return inject(new BackendAttachedStorageDomainResource(id, dataCenterId, SUB_COLLECTIONS));
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

        return performCreate(VdcActionType.AttachStorageDomainToPool,
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
        return getEntity(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                         SearchType.StorageDomain,
                         "Storage: name=" + name).getId();
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomain  lookupStorageDomainById(Guid storageDomainId) {
        return getEntity(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
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
        public org.ovirt.engine.core.common.businessentities.StorageDomain lookupEntity(Guid nullId) {
            assert(nullId == null); // attach action return nothing, lookup original id instead
            return lookupStorageDomainById(storageDomainId);
        }
    }

    @Override
    protected StorageDomain map(org.ovirt.engine.core.common.businessentities.StorageDomain entity, StorageDomain template) {
        BackendStorageDomainsResource resource = new BackendStorageDomainsResource();
        inject(resource);
        return resource.map(entity, template);
    }

    @Override
    protected StorageDomain doPopulate(StorageDomain model, org.ovirt.engine.core.common.businessentities.StorageDomain entity) {
        return model;
    }

    @Override
    public String[] getLinksToExclude(storage_domains storageDomain) {
        return isIsoDomain(storageDomain) ? new String[] { "disks" }
                                            :
                                            new String[] {};
    }
}
