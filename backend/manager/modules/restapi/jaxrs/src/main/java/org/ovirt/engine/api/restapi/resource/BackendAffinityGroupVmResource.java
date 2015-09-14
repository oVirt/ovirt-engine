package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.AffinityGroupVmResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityGroupVmResource
        extends AbstractBackendActionableResource<Vm, org.ovirt.engine.core.common.businessentities.VM>
        implements AffinityGroupVmResource {

    private Guid groupId;

    public BackendAffinityGroupVmResource(Guid groupId, String vmId) {
        super(vmId, Vm.class, org.ovirt.engine.core.common.businessentities.VM.class);
        this.groupId = groupId;
    }

    @Override
    public Response remove() {
        AffinityGroup group = getGroup();
        if (group == null || !group.getEntityIds().remove(asGuid(id))) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return performAction(
            VdcActionType.EditAffinityGroup,
            new AffinityGroupCRUDParameters(groupId, group)
        );
    }

    private org.ovirt.engine.core.common.scheduling.AffinityGroup getGroup() {
        return getEntity(
            org.ovirt.engine.core.common.scheduling.AffinityGroup.class,
            VdcQueryType.GetAffinityGroupById,
            new IdQueryParameters(groupId),
            groupId.toString()
        );
    }
}
