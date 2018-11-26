package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class AbstractBackendStorageDomainDiskResource
        extends AbstractBackendActionableResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk> {

    private static final String UNREGISTERED = "unregistered";

    protected final Guid storageDomainId;

    public AbstractBackendStorageDomainDiskResource(Guid storageDomainId, String diskId) {
        super(diskId, Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.storageDomainId = storageDomainId;
    }

    public Disk get() {
        Disk disk;
        boolean unregistered = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, UNREGISTERED, true, false);
        if (unregistered) {
            QueryReturnValue result = runQuery(QueryType.GetDiskByDiskId, new IdQueryParameters(guid));
            if (!result.getSucceeded() || result.getReturnValue() == null) {
                Guid dataCenterId = BackendDataCenterHelper.lookupByStorageDomainId(this, storageDomainId);
                disk = performGet(
                    QueryType.GetUnregisteredDisk,
                    new GetUnregisteredDiskQueryParameters(guid, storageDomainId, dataCenterId)
                );
            } else {
                // The disk was found in the first get which means it is already registered. We must return nothing
                // since the unregistered parameter was passed.
                return notFound();
            }
        } else {
            disk = getDelegate().get();
        }
        if (unregistered) {
            disk.setActions(null);
        }
        if (disk.isSetStorageDomains() && !disk.getStorageDomains().getStorageDomains().isEmpty()) {
            for (StorageDomain sd : disk.getStorageDomains().getStorageDomains()) {
                if (sd.isSetId() && sd.getId().equals(storageDomainId.toString())) {
                    return disk;
                }
            }
        }
        return notFound();
    }

    @Override
    protected Disk addParents(Disk disk) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setId(storageDomainId.toString());
        StorageDomains storageDomains = new StorageDomains();
        storageDomains.getStorageDomains().add(storageDomain);
        disk.setStorageDomain(storageDomain);
        disk.setStorageDomains(storageDomains);
        return disk;
    }

    public Disk update(Disk disk) {
        return getDelegate().update(disk);
    }

    public Response remove() {
        return getDelegate().remove();
    }

    public StatisticsResource getStatisticsResource() {
        return getDelegate().getStatisticsResource();
    }

    public AssignedPermissionsResource getPermissionsResource() {
        return getDelegate().getPermissionsResource();
    }

    public Response copy(Action action) {
        return getDelegate().copy(action);
    }

    public Response export(Action action) {
        return getDelegate().export(action);
    }

    public Response move(Action action) {
        return getDelegate().move(action);
    }

    public Response sparsify(Action action) {
        return getDelegate().sparsify(action);
    }

    private DiskResource getDelegate() {
        return BackendApiResource.getInstance().getDisksResource().getDiskResource(id);
    }
}
