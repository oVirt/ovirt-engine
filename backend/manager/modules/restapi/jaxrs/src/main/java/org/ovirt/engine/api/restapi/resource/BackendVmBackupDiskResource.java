package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.resource.VmBackupDiskResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendVmBackupDiskResource
        extends AbstractBackendActionableResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements VmBackupDiskResource {

    public BackendVmBackupDiskResource(String diskId) {
        super(diskId, Disk.class, org.ovirt.engine.core.common.businessentities.storage.Disk.class);
    }

    @Override
    public Disk get() {
        return getDisk();
    }

    protected Disk getDisk() {
        return performGet(QueryType.GetDiskByDiskId, new IdQueryParameters(guid));
    }
 }
