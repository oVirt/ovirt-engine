package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.api.model.DiskSnapshots;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.DiskSnapshotResource;
import org.ovirt.engine.api.resource.DiskSnapshotsResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.DiskSnapshotsQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainDiskSnapshotsResource
        extends AbstractBackendCollectionResource<DiskSnapshot, Disk>
        implements DiskSnapshotsResource {

    protected static final String INCLUDE_ACTIVE = "include_active";
    protected static final String INCLUDE_TEMPLATE = "include_template";

    Guid storageDomainId;

    public BackendStorageDomainDiskSnapshotsResource(Guid storageDomainId, String... subCollections) {
        super(DiskSnapshot.class, Disk.class);
        this.storageDomainId = storageDomainId;
    }

    @Override
    public DiskSnapshots list() {
            return mapCollection(getBackendCollection(QueryType.GetAllDiskSnapshotsByStorageDomainId,
                    new DiskSnapshotsQueryParameters(this.storageDomainId, includeActive(), includeTemplate())));
    }

    protected DiskSnapshots mapCollection(List<Disk> entities) {
        DiskSnapshots collection = new DiskSnapshots();
        for (Disk disk : entities) {
            DiskSnapshot diskSnapshot =
                getMapper(Disk.class, DiskSnapshot.class).map(disk, null);

            // this code generates back-link to the corresponding SD
            diskSnapshot.setStorageDomain(new StorageDomain());
            diskSnapshot.getStorageDomain().setId(this.storageDomainId.toString());

            collection.getDiskSnapshots().add(addLinks(populate(diskSnapshot, disk), StorageDomain.class));
        }
        return collection;
    }

    @Override
    public DiskSnapshotResource getSnapshotResource(String id) {
        return inject(new BackendStorageDomainDiskSnapshotResource(id, this));
    }

    protected Guid getStorageDomainId() {
        return storageDomainId;
    }

    private boolean includeActive() {
        return ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, INCLUDE_ACTIVE, true, false);
    }

    private boolean includeTemplate() {
        return ParametersHelper.getBooleanParameter(httpHeaders, uriInfo, INCLUDE_TEMPLATE, true, false);
    }

}
