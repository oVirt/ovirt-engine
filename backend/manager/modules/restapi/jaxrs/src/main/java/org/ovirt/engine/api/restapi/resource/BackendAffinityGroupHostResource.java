package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.resource.AffinityGroupHostResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupCRUDParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityGroupHostResource
        extends AbstractBackendActionableResource<Host, org.ovirt.engine.core.common.businessentities.VDS>
        implements AffinityGroupHostResource {

    private Guid groupId;

    public BackendAffinityGroupHostResource(Guid groupId, String hostId) {
        super(hostId, Host.class, org.ovirt.engine.core.common.businessentities.VDS.class);
        this.groupId = groupId;
    }

    @Override
    public Response remove() {
        AffinityGroup group = getGroup();
        if (group == null || !group.getVdsIds().remove(asGuid(id))) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return performAction(
            ActionType.EditAffinityGroup,
            new AffinityGroupCRUDParameters(groupId, group)
        );
    }

    private org.ovirt.engine.core.common.scheduling.AffinityGroup getGroup() {
        return getEntity(
            org.ovirt.engine.core.common.scheduling.AffinityGroup.class,
            QueryType.GetAffinityGroupById,
            new IdQueryParameters(groupId),
            groupId.toString()
        );
    }
}
