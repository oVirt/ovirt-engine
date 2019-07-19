package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.AffinityLabel;
import org.ovirt.engine.api.resource.AffinityGroupVmLabelResource;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityGroupVmLabelResource
        extends BackendAffinityGroupSubResource<AffinityLabel, org.ovirt.engine.core.common.businessentities.Label>
        implements AffinityGroupVmLabelResource {

    public BackendAffinityGroupVmLabelResource(Guid affinityGroupId, String id) {
        super(affinityGroupId, id, AffinityLabel.class, org.ovirt.engine.core.common.businessentities.Label.class);
    }

    @Override
    public Response remove() {
        return editAffinityGroup(group -> {
            if (!group.getVmLabels().remove(asGuid(id))) {
                throw new WebApplicationException(Response.Status.NOT_FOUND);
            }
        });
    }
}
