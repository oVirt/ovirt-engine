package org.ovirt.engine.api.restapi.resource;

import java.util.Set;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.StorageDomainContentDiskResource;
import org.ovirt.engine.api.resource.StorageDomainContentDisksResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendExportDomainDisksResource
        extends AbstractBackendCollectionResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements StorageDomainContentDisksResource {

    AbstractBackendStorageDomainContentResource parent;

    public BackendExportDomainDisksResource(AbstractBackendStorageDomainContentResource parent) {
        super(Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
        this.parent = parent;
    }

    @Override
    public Disks list() {
        Disks disks = new Disks();
        for (Guid diskImageId : getDiskIds()) {
            disks.getDisks().add(addLinks(map(getDisk(diskImageId))));
        }
        return disks;
    }

    protected Set<Guid> getDiskIds() {
        return parent.getDiskMap().keySet();
    }

    protected org.ovirt.engine.core.common.businessentities.storage.Disk getDisk(Guid id) {
        java.util.Map<Guid, org.ovirt.engine.core.common.businessentities.storage.Disk> map = parent.getDiskMap();
        return map.get(id);
    }

    public AbstractBackendStorageDomainContentResource getParent() {
        return parent;
    }

    @Override
    public StorageDomainContentDiskResource getDiskResource(String id) {
        return inject(new BackendExportDomainDiskResource(id, this));
    }
}
