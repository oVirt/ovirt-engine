package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Disk;
import org.ovirt.engine.api.resource.VmCheckpointDiskResource;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendVmCheckpointDiskResource
        extends AbstractBackendActionableResource<Disk, org.ovirt.engine.core.common.businessentities.storage.Disk>
        implements VmCheckpointDiskResource {

    public BackendVmCheckpointDiskResource(String diskId) {
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
