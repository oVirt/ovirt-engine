package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.resource.CreationResource;
import org.ovirt.engine.api.resource.StorageDomainContentDiskResource;

public class BackendExportDomainDiskResource
        extends AbstractBackendSubResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements StorageDomainContentDiskResource {

    private final BackendExportDomainDisksResource parent;
    private final String diskId;

    public BackendExportDomainDiskResource(
            String diskId,
            BackendExportDomainDisksResource parent) {
        super(diskId, Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.parent = parent;
        this.diskId = diskId;
    }

    @Override
    public Disk get() {
        org.ovirt.engine.core.common.businessentities.storage.Disk disk = parent.getDisk(asGuid(diskId));
        if (disk == null) {
            return notFound();
        }
        return map(disk);
    }

    @Override
    public CreationResource getCreationResource(String ids) {
        return inject(new BackendCreationResource(ids));
    }

    public BackendExportDomainDisksResource getParent() {
        return parent;
    }

}
