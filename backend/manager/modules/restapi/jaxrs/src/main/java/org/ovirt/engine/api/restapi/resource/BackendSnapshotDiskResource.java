package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.resource.SnapshotDiskResource;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;

public class BackendSnapshotDiskResource extends BackendDiskResource implements SnapshotDiskResource {

    protected String diskId;
    protected BackendSnapshotDisksResource collection;

    public BackendSnapshotDiskResource(String diskId, BackendSnapshotDisksResource collection) {
        super(diskId);
        this.diskId = diskId;
        this.collection = collection;
    }

    @Override
    /**
     * Overriding is necessary due to ambiguity: doGet() is inherited
     * from both BackendDiskResource and SnapshotDiskResource interfaces,
     * which both have (the same) default implementation.
     */
    public Disk doGet() {
        return SnapshotDiskResource.super.doGet();
    }

    @Override
    public Disk get() {
        for (Disk disk : collection.list().getDisks()) {
            if (disk.getId().equals(diskId)) {
                return disk;
            }
        }
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
    }

    @Override
    public Response remove() {
        DiskImage diskImage = (DiskImage) DiskMapper.map(get(), null);
        return performAction(ActionType.RemoveDiskSnapshots, new RemoveDiskSnapshotsParameters(diskImage.getImageId()));
    }
}
