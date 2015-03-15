package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.resource.SnapshotDiskResource;
import javax.ws.rs.core.Response;

public class BackendSnapshotDiskResource extends BackendDiskResource implements SnapshotDiskResource {

    protected String diskId;
    protected BackendSnapshotDisksResource collection;

    public BackendSnapshotDiskResource(String diskId, BackendSnapshotDisksResource collection) {
        super(diskId);
        this.diskId = diskId;
        this.collection = collection;
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
    protected Disk doPopulate(Disk model, org.ovirt.engine.core.common.businessentities.storage.Disk entity) {
        return model;
    }
}
