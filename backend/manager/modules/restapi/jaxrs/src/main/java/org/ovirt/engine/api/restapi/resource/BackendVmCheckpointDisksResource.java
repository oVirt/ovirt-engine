package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.VmCheckpointDiskResource;
import org.ovirt.engine.api.resource.VmCheckpointDisksResource;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendVmCheckpointDisksResource
        extends AbstractBackendCollectionResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements VmCheckpointDisksResource {

    private BackendVmCheckpointResource parent;

    public BackendVmCheckpointDisksResource(BackendVmCheckpointResource parent) {
        super(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.parent = parent;
    }

    @Override
    public Disks list() {
        Disks disks = new Disks();
        VmCheckpoint vmCheckpoint = getEntity(VmCheckpoint.class,
                QueryType.GetVmCheckpointById,
                new IdQueryParameters(asGuid(parent.get().getId())),
                null);
        vmCheckpoint.getDisks().stream().map(d -> DiskMapper.map(d, null)).forEach(disks.getDisks()::add);
        return disks;
    }

    @Override
    public VmCheckpointDiskResource getDiskResource(String id) {
        return inject(new BackendVmCheckpointDiskResource(id));
    }
}
