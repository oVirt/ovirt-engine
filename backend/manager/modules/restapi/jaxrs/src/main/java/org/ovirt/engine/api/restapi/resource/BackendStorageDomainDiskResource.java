package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.resource.AssignedPermissionsResource;
import org.ovirt.engine.api.resource.DiskResource;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.resource.StorageDomainDiskResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.queries.GetUnregisteredDiskQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainDiskResource
        extends AbstractBackendActionableResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements StorageDomainDiskResource {

    private final Guid storageDomainId;

    private static final String UNREGISTERED_CONSTRAINT_PARAMETER = "unregistered";

    public BackendStorageDomainDiskResource(Guid storageDomainId, String diskId) {
        super(diskId, Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.storageDomainId = storageDomainId;
    }

    @Override
    public Disk get() {
        Disk disk;
        boolean unregistered = ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, UNREGISTERED_CONSTRAINT_PARAMETER, true, false);
        if (unregistered) {
            QueryReturnValue result = runQuery(QueryType.GetDiskByDiskId, new IdQueryParameters(guid));
            if (!result.getSucceeded() || result.getReturnValue() == null) {
                Guid dataCenterId = BackendDataCenterHelper.lookupByStorageDomainId(this, storageDomainId);
                disk = performGet(
                    QueryType.GetUnregisteredDisk,
                    new GetUnregisteredDiskQueryParameters(guid, storageDomainId, dataCenterId), LinkHelper.NO_PARENT);
            } else {
                // The disk was found in the first get which means it is already registered. We must return nothing since the unregistered
                // parameter was passed.
                return notFound();
            }
        } else {
            disk = getDelegate().get();
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

    @Override
    public Disk update(Disk disk) {
        return getDelegate().update(disk);
    }

    @Override
    public Response remove() {
        return getDelegate().remove();
    }

    @Override
    public StatisticsResource getStatisticsResource() {
        return getDelegate().getStatisticsResource();
    }

    @Override
    public AssignedPermissionsResource getPermissionsResource() {
        return getDelegate().getPermissionsResource();
    }

    @Override
    public Response copy(Action action) {
        return getDelegate().copy(action);
    }

    @Override
    public Response export(Action action) {
        return getDelegate().export(action);
    }

    @Override
    public Response move(Action action) {
        return getDelegate().move(action);
    }

    @Override
    public Response sparsify(Action action) {
        return getDelegate().sparsify(action);
    }

    @Override
    public Response reduce(Action action) {
        return getDelegate().reduce(action);
    }

    private DiskResource getDelegate() {
        return BackendApiResource.getInstance().getDisksResource().getDiskResource(id);
    }
}
