package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.resource.AffinityGroupVmResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public class BackendAffinityGroupVmResource
        extends AbstractBackendActionableResource<VM, org.ovirt.engine.core.common.businessentities.VM>
        implements AffinityGroupVmResource {

    private Guid groupId;

    public BackendAffinityGroupVmResource(Guid groupId, String vmId) {
        super(vmId, VM.class, org.ovirt.engine.core.common.businessentities.VM.class);
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

    @Override
    protected VM doPopulate(VM model, org.ovirt.engine.core.common.businessentities.VM entity) {
        return model;
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
