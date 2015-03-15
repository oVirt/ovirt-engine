package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.resource.SnapshotDiskResource;
import org.ovirt.engine.api.resource.SnapshotDisksResource;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;

import javax.ws.rs.core.Response;

public class BackendSnapshotDisksResource extends AbstractBackendCollectionResource<Disk, Snapshot>  implements SnapshotDisksResource {

    protected BackendSnapshotResource parent;

    public BackendSnapshotDisksResource(BackendSnapshotResource parent) {
        super(Disk.class, Snapshot.class);
        this.parent = parent;
    }

    @Override
    public Disks list() {
        Disks disks = new Disks();
        if (parent.getSnapshot().isVmConfigurationAvailable()) {
            VM vm = parent.collection.getVmPreview(parent.get());
            for (DiskImage disk : vm.getDiskList()) {
                Disk d = DiskMapper.map(disk, null);
                map(d, parent.id);
                disks.getDisks().add(d);
            }
        }
        return disks;
    }

    public void map(Disk disk, String snapshotId) {
        disk.setSnapshot(new Snapshot());
        disk.getSnapshot().setId(snapshotId);
    }

    @Override
    public SnapshotDiskResource getDiskSubResource(String id) {
        return new BackendSnapshotDiskResource(id, this);
    }

    @Override
    public Response performRemove(String id) {
        getEntity(id); //verifies that entity exists, returns 404 otherwise.

        DiskImage diskImage = (DiskImage) DiskMapper.map(getDiskSubResource(id).get(), null);

        return performAction(VdcActionType.RemoveDiskSnapshots, new RemoveDiskSnapshotsParameters(diskImage.getImageId()));
    }

    @Override
    protected Disk doPopulate(Disk model, Snapshot entity) {
        return model;
    }
}
