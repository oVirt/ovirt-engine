/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Checkpoint;
import org.ovirt.engine.api.resource.VmCheckpointDisksResource;
import org.ovirt.engine.api.resource.VmCheckpointResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VmCheckpointParameters;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmCheckpointResource
        extends AbstractBackendSubResource<Checkpoint, VmCheckpoint>
        implements VmCheckpointResource {

    private BackendVmCheckpointsResource parent;
    private Guid vmId;

    public BackendVmCheckpointResource(String vmCheckpointId, Guid vmId, BackendVmCheckpointsResource parent) {
        super(vmCheckpointId, Checkpoint.class, VmCheckpoint.class);
        this.vmId = vmId;
        this.parent = parent;
    }

    public BackendVmCheckpointsResource getParent() {
        return parent;
    }

    @Override
    public Checkpoint get() {
        return addLinks(performGet(QueryType.GetVmCheckpointById, new IdQueryParameters(guid)));
    }

    @Override
    public Response remove() {
        get();
        VmCheckpoint vmCheckpoint = new VmCheckpoint();
        vmCheckpoint.setId(asGuid(id));
        vmCheckpoint.setVmId(vmId);
        VmCheckpointParameters vmCheckpointParameters = new VmCheckpointParameters(vmCheckpoint);
        return performAction(ActionType.DeleteVmCheckpoint, vmCheckpointParameters);
    }

    @Override
    public VmCheckpointDisksResource getDisksResource() {
        return inject(new BackendVmCheckpointDisksResource(this));
    }
}
