package org.ovirt.engine.api.restapi.resource;


import java.util.List;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.DiskSnapshot;
import org.ovirt.engine.api.model.DiskSnapshots;
import org.ovirt.engine.api.resource.DiskSnapshotResource;
import org.ovirt.engine.api.resource.DiskSnapshotsResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDiskSnapshotsResource
        extends AbstractBackendCollectionResource<DiskSnapshot, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements DiskSnapshotsResource {

    protected Guid diskId;

    public BackendDiskSnapshotsResource(Guid diskId) {
        super(DiskSnapshot.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.diskId = diskId;
    }

    @Override
    public DiskSnapshots list() {
        return mapCollection(getBackendCollection(QueryType.GetAllDiskSnapshots,  new IdQueryParameters(diskId)));
    }

    protected DiskSnapshots mapCollection(List<org.ovirt.engine.core.common.businessentities.storage.Disk> entities) {
        DiskSnapshots collection = new DiskSnapshots();
        for (org.ovirt.engine.core.common.businessentities.storage.Disk disk : entities) {
            DiskSnapshot diskSnapshot = getMapper(org.ovirt.engine.core.common.businessentities.storage.Disk.class, DiskSnapshot.class).map(disk, null);
            diskSnapshot.setDisk(new Disk());
            diskSnapshot.getDisk().setId(this.diskId.toString());
            collection.getDiskSnapshots().add(addLinks(populate(diskSnapshot, disk), Disk.class));
            diskSnapshot.setHref(buildHref(diskId.toString(), diskSnapshot.getId().toString()));
            if (diskSnapshot.getParent() != null) {
                diskSnapshot.getParent().setHref(buildParentHref(diskId.toString(), false));
            }
        }
        return collection;
    }

    protected String buildHref(String diskId, String snapshotId) {
        return  "/ovirt-engine/api/disks/" + diskId + "/disksnapshots/" + snapshotId;
    }

    protected String buildParentHref(String diskId, boolean addDiskSnapshots) {
        return  "/ovirt-engine/api/disks/" + diskId + (addDiskSnapshots ? "/disksnapshots" :"");
    }

    @Override
    public DiskSnapshotResource getSnapshotResource(String id) {
        return inject(new BackendDiskSnapshotResource(id, this));
    }
}
