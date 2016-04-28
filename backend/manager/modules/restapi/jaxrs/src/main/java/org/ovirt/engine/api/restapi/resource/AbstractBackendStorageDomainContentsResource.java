package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendDataCenterResource.getStoragePools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendStorageDomainContentsResource<C extends BaseResources,
                                                                   R extends BaseResource,
                                                                   Q extends IVdcQueryable>
    extends AbstractBackendCollectionResource<R, Q> {
    protected Guid storageDomainId;
    protected static final String UNREGISTERED_CONSTRAINT_PARAMETER = "unregistered";

    public AbstractBackendStorageDomainContentsResource(Guid storageDomainId,
                                                        Class<R> modelType,
                                                        Class<Q> entityType,
                                                        String... subCollections) {
        super(modelType, entityType, subCollections);
        this.storageDomainId = storageDomainId;
    }

    protected Guid getDataCenterId(Action action) {
        return getStoragePoolId(action);
    }

    public Guid getStoragePoolId(Action action) {
        if(action.getStorageDomain().isSetId()){
            return getDataCenterId(Guid.createGuidFromStringDefaultEmpty(action.getStorageDomain().getId()));
        } else {
            return getDataCenterId(lookupStorageDomainIdByName(action.getStorageDomain().getName()));
        }
    }

    protected Guid lookupStorageDomainIdByName(String name) {
        return getEntity(org.ovirt.engine.core.common.businessentities.StorageDomainStatic.class,
                VdcQueryType.GetStorageDomainByName,
                new NameQueryParameters(name),
                "Storage: name=" + name).getId();

    }

    public Guid getDataCenterId(Guid storageDomainId) {
        List<StoragePool> storagepools = getStoragePools(storageDomainId, this);
        return storagepools.size() > 0 ?
                storagepools.get(0).getId()
                :
                null;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public org.ovirt.engine.core.common.businessentities.StorageDomain getStorageDomain() {
        return getEntity(org.ovirt.engine.core.common.businessentities.StorageDomain.class,
                         VdcQueryType.GetStorageDomainById,
                         new IdQueryParameters(storageDomainId),
                         storageDomainId.toString());
    }

    public StorageDomainType getStorageDomainType() {
        return getStorageDomain().getStorageDomainType();
    }

    public StorageDomain getStorageDomainModel() {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId.toString());
        return storageDomain;
    }

    public List<R> getCollection() {
        return getCollection(getStorageDomainType());
    }

    public List<R> getCollection(StorageDomainType storageDomainType) {
        Collection<Q> entities = new ArrayList<>();

        switch (storageDomainType) {
        case Data:
        case Master:
            break;
        case ImportExport:
            entities = getEntitiesFromExportDomain();
            break;
        case ISO:
        case Unknown:
        }

        List<R> collection = new ArrayList<>();
        for (Q entity : entities) {
            collection.add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    protected abstract Collection<Q> getEntitiesFromExportDomain();
}
