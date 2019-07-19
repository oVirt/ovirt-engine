package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.AffinityLabel;
import org.ovirt.engine.api.resource.AffinityGroupHostLabelResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityGroupHostLabelResource
        extends BackendAffinityGroupSubResource<AffinityLabel, org.ovirt.engine.core.common.businessentities.Label>
        implements AffinityGroupHostLabelResource {

    public BackendAffinityGroupHostLabelResource(Guid affinityGroupId, String id) {
        super(affinityGroupId, id, AffinityLabel.class, org.ovirt.engine.core.common.businessentities.Label.class);
    }

    @Override
    public Response remove() {
        return editAffinityGroup(group -> {
            if (!group.getHostLabels().remove(asGuid(id))) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        });
    }
}
