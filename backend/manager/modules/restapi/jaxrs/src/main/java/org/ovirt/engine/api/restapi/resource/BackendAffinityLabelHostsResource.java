package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.resource.AffinityLabelHostResource;
import org.ovirt.engine.api.resource.AffinityLabelHostsResource;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.businessentities.VDS;

public class BackendAffinityLabelHostsResource
    extends AbstractBackendCollectionResource<Host, VDS>
    implements AffinityLabelHostsResource {

    private String labelId;

    public BackendAffinityLabelHostsResource(String labelId) {
        super(Host.class, VDS.class);
        this.labelId = labelId;
    }

    @Override
    public Response add(Host model) {
        validateParameters(model, "id");

        Label label = BackendAffinityLabelHelper.getLabel(this, labelId);

        VDS entity = new VDS();
        entity.setId(GuidUtils.asGuid(model.getId()));

        Label updatedLabel = new LabelBuilder(label)
            .entity(entity)
            .build();

        // The command used to add the host to the label returns the label, but we need to return the virtual machine,
        // so we ignore the result and return a link to the added host:
        LabelActionParameters updateParams = new LabelActionParameters(updatedLabel);
        try {
            doAction(ActionType.UpdateLabel, updateParams);
        } catch (BackendFailureException exception) {
            handleError(exception, false);
        }
        Host result = BackendAffinityLabelHelper.makeHostLink(entity.getId());
        return Response.ok(Response.Status.CREATED).entity(result).build();
    }

    @Override
    public Hosts list() {
        Hosts hosts = new Hosts();
        List<Host> list = hosts.getHosts();
        Label label = BackendAffinityLabelHelper.getLabel(this, labelId);
        label.getHosts().stream()
            .map(BackendAffinityLabelHelper::makeHostLink)
            .forEach(list::add);
        return hosts;
    }

    @Override
    public AffinityLabelHostResource getHostResource(String id) {
        return inject(new BackendAffinityLabelHostResource(labelId, id));
    }
}
