package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendStorageDomainResource.isIsoDomain;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.resource.AttachedStorageDomainResource;
import org.ovirt.engine.api.resource.AttachedStorageDomainsResource;
import org.ovirt.engine.api.restapi.util.StorageDomainHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachStorageDomainToPoolParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.StorageDomainAndPoolQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendAttachedStorageDomainsResource
    extends AbstractBackendCollectionResource<StorageDomain, org.ovirt.engine.core.common.businessentities.StorageDomain>
    implements AttachedStorageDomainsResource {

    protected Guid dataCenterId;

    public BackendAttachedStorageDomainsResource(String dataCenterId) {
        super(StorageDomain.class, org.ovirt.engine.core.common.businessentities.StorageDomain.class);
        this.dataCenterId = asGuid(dataCenterId);
    }

    @Override
    public StorageDomains list() {
        StorageDomains storageDomains = new StorageDomains();

        for (org.ovirt.engine.core.common.businessentities.StorageDomain entity : getBackendCollection(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                                                           QueryType.GetStorageDomainsByStoragePoolId,
                new IdQueryParameters(dataCenterId))) {
            storageDomains.getStorageDomains().add(addLinks(map(entity), getLinksToExclude(entity)));
        }

        return storageDomains;
    }

    @Override
    public AttachedStorageDomainResource getStorageDomainResource(String id) {
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

        return performCreate(ActionType.AttachStorageDomainToPool,
                               new AttachStorageDomainToPoolParameters(storageDomainId, dataCenterId),
                               new StorageDomainIdResolver(storageDomainId));
    }

    @Override
    protected StorageDomain addParents(StorageDomain storageDomain) {
        // This is for backwards compatibility and will be removed in the future:
        storageDomain.setDataCenter(new DataCenter());
        storageDomain.getDataCenter().setId(dataCenterId.toString());

        // Find all the data centers that this storage domain is attached to and add references to them:
        StorageDomainHelper.addAttachedDataCenterReferences(this, storageDomain);

        return storageDomain;
    }

    protected Guid lookupStorageDomainIdByName(String name) {
        return getEntity(org.ovirt.engine.core.common.businessentities.StorageDomainStatic.class,
                QueryType.GetStorageDomainByName,
                new NameQueryParameters(name),
                "Storage: name=" + name).getId();
    }

    protected org.ovirt.engine.core.common.businessentities.StorageDomain  lookupStorageDomainById(Guid storageDomainId) {
        return getEntity(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                         QueryType.GetStorageDomainByIdAndStoragePoolId,
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
            assert nullId == null; // attach action return nothing, lookup original id instead
            return lookupStorageDomainById(storageDomainId);
        }
    }

    @Override
    protected StorageDomain map(org.ovirt.engine.core.common.businessentities.StorageDomain entity, StorageDomain template) {
        BackendStorageDomainsResource resource = new BackendStorageDomainsResource();
        inject(resource);
        return resource.map(entity, template);
    }

    public String[] getLinksToExclude(org.ovirt.engine.core.common.businessentities.StorageDomain storageDomain) {
        return isIsoDomain(storageDomain) ? new String[] { "disks" }
                                            :
                                            new String[] {};
    }
}
