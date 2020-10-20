package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.api.resource.DiskSnapshotResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDiskSnapshotResource
        extends AbstractBackendActionableResource<DiskSnapshot, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements DiskSnapshotResource {

    private Guid diskId;
    private BackendDiskSnapshotsResource backendDiskSnapshotsResource;

    protected BackendDiskSnapshotResource(String id, BackendDiskSnapshotsResource parent) {
        super(id, DiskSnapshot.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.diskId = parent.diskId;
        this.backendDiskSnapshotsResource = parent;
    }

    @Override
    public DiskSnapshot get() {
        DiskSnapshot diskSnapshot = performGet(QueryType.GetAllDiskSnapshots, new IdQueryParameters(diskId), Disk.class);
        diskSnapshot.setDisk(new Disk());
        diskSnapshot.getDisk().setId(diskId.toString());
        diskSnapshot.setHref(backendDiskSnapshotsResource.buildHref(diskId.toString(), diskSnapshot.getId().toString()));
        if (diskSnapshot.getParent() != null) {
            diskSnapshot.getParent().setHref(backendDiskSnapshotsResource.buildParentHref(diskId.toString(), true));
        }
        return diskSnapshot;
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveDiskSnapshots, new RemoveDiskSnapshotsParameters(guid));
    }

}
