package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.DiskSnapshotResource;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendStorageDomainDiskSnapshotResource
        extends AbstractBackendActionableResource<DiskSnapshot, Disk>
        implements DiskSnapshotResource {

    final private String storageDomainId;

    protected BackendStorageDomainDiskSnapshotResource(String id, BackendStorageDomainDiskSnapshotsResource parent) {
        super(id, DiskSnapshot.class, Disk.class);
        this.storageDomainId = parent.getStorageDomainId().toString();
    }

    @Override
    protected DiskSnapshot doPopulate(DiskSnapshot model, Disk entity) {
        return model;
    }

    public String getStorageDomainId() {
        return storageDomainId;
    }

    @Override
    public DiskSnapshot get() {
        DiskSnapshot diskSnapshot =  performGet(VdcQueryType.GetDiskSnapshotByImageId, new IdQueryParameters(guid));

        // this code generates back-link to the corresponding SD
        diskSnapshot.setStorageDomain(new StorageDomain());
        diskSnapshot.getStorageDomain().setId(this.storageDomainId);

        return diskSnapshot;
    }

}
