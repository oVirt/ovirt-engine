package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.api.model.StorageDomain;
import org.ovirt.engine.api.resource.DiskSnapshotResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendStorageDomainDiskSnapshotResource
        extends AbstractBackendActionableResource<DiskSnapshot, Disk>
        implements DiskSnapshotResource {

    private final String storageDomainId;

    protected BackendStorageDomainDiskSnapshotResource(String id, BackendStorageDomainDiskSnapshotsResource parent) {
        super(id, DiskSnapshot.class, Disk.class);
        this.storageDomainId = parent.getStorageDomainId().toString();
    }

    public String getStorageDomainId() {
        return storageDomainId;
    }

    @Override
    public DiskSnapshot get() {
        DiskSnapshot diskSnapshot =  performGet(QueryType.GetDiskSnapshotByImageId, new IdQueryParameters(guid), StorageDomain.class);

        // this code generates back-link to the corresponding SD
        diskSnapshot.setStorageDomain(new StorageDomain());
        diskSnapshot.getStorageDomain().setId(this.storageDomainId);
        diskSnapshot.setHref(buildHref(storageDomainId, diskSnapshot.getId().toString()));

        return diskSnapshot;
    }

    private String buildHref(String storageDomainId, String snapshotId) {
        return "/ovirt-engine/api/storagedomains/" + storageDomainId + "/disksnapshots/" + snapshotId;
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveDiskSnapshots, new RemoveDiskSnapshotsParameters(guid));
    }
}
