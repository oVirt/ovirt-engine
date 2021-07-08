package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.resource.AffinityGroupHostResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.scheduling.parameters.AffinityGroupMemberChangeParameters;
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
        return performAction(ActionType.RemoveHostFromAffinityGroup,
                new AffinityGroupMemberChangeParameters(groupId, asGuid(id)));
    }
}
