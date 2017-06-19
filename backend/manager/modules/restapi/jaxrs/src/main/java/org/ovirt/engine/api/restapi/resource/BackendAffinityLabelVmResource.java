package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.AffinityLabelVmResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.businessentities.VM;

public class BackendAffinityLabelVmResource
    extends AbstractBackendActionableResource<Vm, VM>
    implements AffinityLabelVmResource {

    private String labelId;

    public BackendAffinityLabelVmResource(String labelId, String id) {
        super(id, Vm.class, VM.class);
        this.labelId = labelId;
    }

    public Vm get() {
        // First we need to check if the affinity label does contain the virtual machine:
        Label label = BackendAffinityLabelHelper.getLabel(this, labelId);
        if (!label.getVms().contains(guid)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // Then we return a link to the virtual machine:
        return BackendAffinityLabelHelper.makeVmLink(guid);
    }

    public Response remove() {
        // First we need to check if the affinity label does contain the virtual machine:
        Label label = BackendAffinityLabelHelper.getLabel(this, labelId);
        if (!label.getVms().contains(guid)) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }

        // Remove the virtual machine from the label:
        VM entity = new VM();
        entity.setId(guid);
        Label updatedLabel = new LabelBuilder(label)
            .removeEntity(entity)
            .build();
        return performAction(ActionType.UpdateLabel, new LabelActionParameters(updatedLabel));
    }
}
