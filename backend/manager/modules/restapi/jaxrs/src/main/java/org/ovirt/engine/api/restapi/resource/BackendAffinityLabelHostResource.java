package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.resource.AffinityLabelHostResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.businessentities.VDS;

public class BackendAffinityLabelHostResource
    extends AbstractBackendActionableResource<Host, VDS>
    implements AffinityLabelHostResource {

    private String labelId;

    public BackendAffinityLabelHostResource(String labelId, String id) {
        super(id, Host.class, VDS.class);
        this.labelId = labelId;
    }

    public Host get() {
        // First we need to check if the label contains the host:
        Label label = BackendAffinityLabelHelper.getLabel(this, labelId);
        if (!label.getHosts().contains(guid)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // Then we return a link to the host:
        return BackendAffinityLabelHelper.makeHostLink(guid);
    }

    public Response remove() {
        // First we need to check if the label contains the host:
        Label label = BackendAffinityLabelHelper.getLabel(this, labelId);
        if (!label.getHosts().contains(guid)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // Remove the host from the label:
        VDS entity = new VDS();
        entity.setId(guid);
        Label updatedLabel = new LabelBuilder(label)
            .removeEntity(entity)
            .build();
        return performAction(ActionType.UpdateLabel, new LabelActionParameters(updatedLabel));
    }
}
