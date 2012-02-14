package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.GetVdsGroupByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendStorageDomainContentResource<C extends BaseResources,
                                                                  R extends BaseResource,
                                                                  Q extends IVdcQueryable>
    extends AbstractBackendActionableResource<R, Q> {

    protected AbstractBackendStorageDomainContentsResource<C, R, Q> parent;

    public AbstractBackendStorageDomainContentResource(String id,
                                                       AbstractBackendStorageDomainContentsResource<C, R, Q>  parent,
                                                       Class<R> modelType,
                                                       Class<Q> entityType) {
        super(id, modelType, entityType);
        this.parent = parent;
    }

    public AbstractBackendStorageDomainContentsResource<C, R, Q> getParent() {
        return parent;
    }

    public R get() {
        switch (parent.getStorageDomainType()) {
        case Data:
        case Master:
            return getFromDataDomain();
        case ImportExport:
            return getFromExportDomain();
        case ISO:
        case Unknown:
        default:
            return null;
        }
    }

    protected abstract R getFromDataDomain();

    protected R getFromExportDomain() {
        for (R model : parent.getCollection(StorageDomainType.ImportExport)) {
            if (model.getId().equals(id)) {
                return model;
            }
        }
        return notFound();
    }

    protected Guid getDestStorageDomainId(Action action) {
        if (action.getStorageDomain().isSetId()) {
            return asGuid(action.getStorageDomain().getId());
        } else {
            return parent.lookupStorageDomainIdByName(action.getStorageDomain().getName());
        }
    }

    protected Guid getClusterId(Action action) {
        if (action.getCluster().isSetId()) {
            return asGuid(action.getCluster().getId());
        } else {
            return lookupClusterIdByName(action.getCluster().getName());
        }
    }

    protected Guid lookupClusterIdByName(String name) {
        return lookupClusterByName(name).getId();
    }

    protected VDSGroup lookupClusterByName(String name) {
        return getEntity(VDSGroup.class, SearchType.Cluster, "Cluster: name=" + name);
    }

    protected VDSGroup lookupClusterById(String id) {
        return getEntity(VDSGroup.class,
                         VdcQueryType.GetVdsGroupById,
                         new GetVdsGroupByIdParameters(Guid.createGuidFromString(id)),
                         id);
    }
}
