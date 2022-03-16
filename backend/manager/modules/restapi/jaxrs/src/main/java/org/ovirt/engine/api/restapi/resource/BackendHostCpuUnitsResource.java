package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.HostCpuUnit;
import org.ovirt.engine.api.resource.HostCpuUnitsResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostCpuUnitsResource extends AbstractBackendCollectionResource<HostCpuUnit, Object> implements HostCpuUnitsResource {

    private Guid hostId;

    public BackendHostCpuUnitsResource(Guid hostId) {
        super(HostCpuUnit.class, Object.class);
        this.hostId = hostId;
    }
}
