package org.ovirt.engine.api.restapi.resource;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskAttachment;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendStorageDomainContentResource<C extends BaseResources,
                                                                  R extends BaseResource,
                                                                  Q extends Queryable>
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

    protected abstract Map<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> getDiskMap();

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

    protected Cluster lookupClusterByName(String name) {
        return getEntity(Cluster.class,
                QueryType.GetClusterByName,
                new NameQueryParameters(name),
                "Cluster: name=" + name);
    }

    protected Map<Guid, Guid> getDiskToDestinationMap(Action action) {
        Map<Guid, Guid> diskToDestinationMap = new HashMap<>();
        if (action.isSetVm() && action.getVm().isSetDiskAttachments() && action.getVm().getDiskAttachments().isSetDiskAttachments()) {
            for (DiskAttachment diskAttachment : action.getVm().getDiskAttachments().getDiskAttachments()) {
                Disk disk = diskAttachment.getDisk();
                if (disk != null && disk.isSetId() && disk.isSetStorageDomains() && disk.getStorageDomains().isSetStorageDomains()
                        && disk.getStorageDomains().getStorageDomains().get(0).isSetId()) {
                    diskToDestinationMap.put(Guid.createGuidFromStringDefaultEmpty(disk.getId()),
                            Guid.createGuidFromStringDefaultEmpty(disk.getStorageDomains().getStorageDomains().get(0).getId()));
                }
            }
        }
        return diskToDestinationMap;
    }
}
