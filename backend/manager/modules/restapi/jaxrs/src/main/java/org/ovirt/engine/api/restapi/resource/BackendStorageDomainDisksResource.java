package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Disks;
import org.ovirt.engine.api.resource.StorageDomainDiskResource;
import org.ovirt.engine.api.resource.StorageDomainDisksResource;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.core.compat.Guid;

public class BackendStorageDomainDisksResource
        extends AbstractBackendStorageDomainDisksResource
        implements StorageDomainDisksResource {

    public BackendStorageDomainDisksResource(Guid storageDomainId) {
        super(storageDomainId);
    }

    @Override
    public StorageDomainDiskResource getDiskResource(String diskId) {
        return inject(new BackendStorageDomainDiskResource(storageDomainId, diskId));
    }

    @Override
    protected Disks mapCollection(List<org.ovirt.engine.core.common.businessentities.storage.Disk> entities) {
        Disks collection = new Disks();
        for (org.ovirt.engine.core.common.businessentities.storage.Disk disk : entities) {
            collection.getDisks().add(addLinks(populate(map(disk), disk), LinkHelper.NO_PARENT));
        }
        return collection;
    }
}
