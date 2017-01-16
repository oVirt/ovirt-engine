package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.resource.StorageDomainDiskResource;
import org.ovirt.engine.api.resource.StorageDomainDisksResource;
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
}
