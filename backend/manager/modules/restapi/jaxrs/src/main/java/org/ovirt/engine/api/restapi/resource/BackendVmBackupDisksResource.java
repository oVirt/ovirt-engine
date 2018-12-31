package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.VmBackupDiskResource;
import org.ovirt.engine.api.resource.VmBackupDisksResource;
import org.ovirt.engine.api.restapi.types.DiskMapper;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendVmBackupDisksResource
        extends AbstractBackendCollectionResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements VmBackupDisksResource {

    private BackendVmBackupResource parent;

    public BackendVmBackupDisksResource(BackendVmBackupResource parent) {
        super(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.parent = parent;
    }

    @Override
    public Disks list() {
        Disks disks = new Disks();
        VmBackup vmBackup = getEntity(VmBackup.class, QueryType.GetVmBackupById, new IdQueryParameters(asGuid(parent.get().getId())), null);
        vmBackup.getDisks().stream().map(d -> DiskMapper.map(d, null)).forEach(disks.getDisks()::add);
        return disks;
    }

    @Override
    public VmBackupDiskResource getDiskResource(String id) {
        return inject(new BackendVmBackupDiskResource(id));
    }
}
