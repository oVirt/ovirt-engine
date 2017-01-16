package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.resource.AttachedStorageDomainDiskResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendAttachedStorageDomainDiskResource
        extends AbstractBackendStorageDomainDiskResource
        implements AttachedStorageDomainDiskResource {

    public BackendAttachedStorageDomainDiskResource(Guid storageDomainId, String diskId) {
        super(storageDomainId, diskId);
    }
}
