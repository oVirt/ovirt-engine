package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.AffinityGroupVmResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityGroupVmResource
        extends BackendAffinityGroupSubResource<Vm, org.ovirt.engine.core.common.businessentities.VM>
        implements AffinityGroupVmResource {

    public BackendAffinityGroupVmResource(Guid groupId, String vmId) {
        super(groupId, vmId, Vm.class, org.ovirt.engine.core.common.businessentities.VM.class);
    }

    @Override
    public Response remove() {
        return editAffinityGroup(group -> {
            if (!group.getVmIds().remove(asGuid(id))) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        });
    }
}
