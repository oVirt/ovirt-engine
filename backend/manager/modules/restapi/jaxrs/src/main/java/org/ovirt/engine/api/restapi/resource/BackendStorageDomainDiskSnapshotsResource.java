package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.api.model.DiskSnapshots;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.DiskSnapshotResource;
import org.ovirt.engine.api.resource.DiskSnapshotsResource;
import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

public class BackendStorageDomainDiskSnapshotsResource
        extends AbstractBackendCollectionResource<DiskSnapshot, org.ovirt.engine.core.common.businessentities.Disk>
        implements DiskSnapshotsResource {

    Guid storageDomainId;

    public BackendStorageDomainDiskSnapshotsResource(Guid storageDomainId, String... subCollections) {
        super(DiskSnapshot.class, org.ovirt.engine.core.common.businessentities.Disk.class);
        this.storageDomainId = storageDomainId;
    }

    @Override
    public DiskSnapshots list() {
            return mapCollection(getBackendCollection(VdcQueryType.GetAllDiskSnapshotsByStorageDomainId,
                    new IdQueryParameters(this.storageDomainId)));
    }

    protected DiskSnapshots mapCollection(List<org.ovirt.engine.core.common.businessentities.Disk> entities) {
        DiskSnapshots collection = new DiskSnapshots();
        for (org.ovirt.engine.core.common.businessentities.Disk disk : entities) {
            DiskSnapshot diskSnapshot =
                getMapper(org.ovirt.engine.core.common.businessentities.Disk.class, DiskSnapshot.class).map(disk, null);

            // this code generates back-link to the corresponding SD
            diskSnapshot.setStorageDomain(new StorageDomain());
            diskSnapshot.getStorageDomain().setId(this.storageDomainId.toString());

            collection.getDiskSnapshots().add(addLinks(populate(diskSnapshot, disk)));
        }
        return collection;
    }

    @Override
    protected Response performRemove(String id) {
        return performAction(VdcActionType.RemoveDiskSnapshots, new RemoveDiskSnapshotsParameters(Guid.createGuidFromString(id)));
    }

    @Override
    public DiskSnapshotResource getDeviceSubResource(String id) {
        return inject(new BackendStorageDomainDiskSnapshotResource(id, this));
    }

    @Override
    protected DiskSnapshot doPopulate(DiskSnapshot model, org.ovirt.engine.core.common.businessentities.Disk entity) {
        return model;
    }

    protected Guid getStorageDomainId() {
        return storageDomainId;
    }
}
