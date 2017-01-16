package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.resource.AttachedStorageDomainDiskResource;
import org.ovirt.engine.api.resource.AttachedStorageDomainDisksResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendAttachedStorageDomainDisksResource
        extends AbstractBackendStorageDomainDisksResource
        implements AttachedStorageDomainDisksResource {

    public BackendAttachedStorageDomainDisksResource(Guid storageDomainId) {
        super(storageDomainId);
    }

    @Override
    public AttachedStorageDomainDiskResource getDiskResource(String diskId) {
        return inject(new BackendAttachedStorageDomainDiskResource(storageDomainId, diskId));
    }
}
