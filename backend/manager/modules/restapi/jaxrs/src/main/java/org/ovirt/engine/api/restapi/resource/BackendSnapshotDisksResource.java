package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.model.Snapshot;
import org.ovirt.engine.api.resource.SnapshotDiskResource;
import org.ovirt.engine.api.resource.SnapshotDisksResource;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;

public class BackendSnapshotDisksResource extends BackendSnapshotElementsResource implements SnapshotDisksResource {

    public BackendSnapshotDisksResource(BackendSnapshotResource parent, String vmId) {
        super(parent, vmId);
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
}
