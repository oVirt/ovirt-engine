package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.resource.AffinityLabelVmResource;
import org.ovirt.engine.api.resource.AffinityLabelVmsResource;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.businessentities.VM;

public class BackendAffinityLabelVmsResource
    extends AbstractBackendCollectionResource<Vm, VM>
    implements AffinityLabelVmsResource {

    private String labelId;

    public BackendAffinityLabelVmsResource(String labelId) {
        super(Vm.class, VM.class);
        this.labelId = labelId;
    }

    @Override
    public Response add(Vm model) {
        validateParameters(model, "id");

        Label label = BackendAffinityLabelHelper.getLabel(this, labelId);

        VM entity = new VM();
        entity.setId(GuidUtils.asGuid(model.getId()));

        Label updatedLabel = new LabelBuilder(label)
            .entity(entity)
            .build();

        // The command used to add the virtual machine to the label returns the label, but we need to return the virtual
        // machine, so we ignore the result and return a link to the added virtual machine:
        LabelActionParameters updateParams = new LabelActionParameters(updatedLabel);
        try {
            doAction(ActionType.UpdateLabel, updateParams);
        } catch (BackendFailureException exception) {
            handleError(exception, false);
        }
        Vm result = BackendAffinityLabelHelper.makeVmLink(entity.getId());
        return Response.ok(Response.Status.CREATED).entity(result).build();
    }

    @Override
    public Vms list() {
        Vms vms = new Vms();
        List<Vm> list = vms.getVms();
        Label label = BackendAffinityLabelHelper.getLabel(this, labelId);
        label.getVms().stream()
            .map(BackendAffinityLabelHelper::makeVmLink)
            .forEach(list::add);
        return vms;
    }

    @Override
    public AffinityLabelVmResource getVmResource(String id) {
        return inject(new BackendAffinityLabelVmResource(labelId, id));
    }
}
