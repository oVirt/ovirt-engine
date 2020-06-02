/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Checkpoint;
import org.ovirt.engine.api.model.Checkpoints;
import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.VmCheckpointResource;
import org.ovirt.engine.api.resource.VmCheckpointsResource;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmCheckpointsResource
        extends AbstractBackendCollectionResource<Checkpoint, VmCheckpoint>
        implements VmCheckpointsResource {

    private Guid vmId;

    public BackendVmCheckpointsResource(Guid vmId) {
        super(Checkpoint.class, VmCheckpoint.class);
        this.vmId = vmId;
    }

    @Override
    public Checkpoint addParents(Checkpoint checkpoint) {
        Vm vm = new Vm();
        vm.setId(vmId.toString());
        checkpoint.setVm(vm);
        return checkpoint;
    }

    @Override
    public Checkpoints list() {
        List<VmCheckpoint> entities =
                getBackendCollection(QueryType.GetAllVmCheckpointsByVmId, new IdQueryParameters(vmId));
        Checkpoints collection = new Checkpoints();
        entities.forEach(entity -> collection.getCheckpoints().add(addLinks(map(entity), Vm.class)));
        return collection;
    }

    @Override
    public VmCheckpointResource getCheckpointResource(String id) {
        return inject(new BackendVmCheckpointResource(id, vmId, this));
    }
}
