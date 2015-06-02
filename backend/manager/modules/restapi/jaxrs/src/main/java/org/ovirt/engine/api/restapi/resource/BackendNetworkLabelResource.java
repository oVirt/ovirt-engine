package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Label;
import org.ovirt.engine.api.model.Labels;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.LabelResource;
import org.ovirt.engine.core.common.action.UnlabelNetworkParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;

import javax.ws.rs.core.Response;

public class BackendNetworkLabelResource extends AbstractBackendSubResource<Label, NetworkLabel> implements LabelResource {

    private BackendNetworkLabelsResource parent;

    protected BackendNetworkLabelResource(String id, BackendNetworkLabelsResource parent) {
        super("", Label.class, NetworkLabel.class);
        this.id = id;
        this.parent = parent;
    }

    public BackendNetworkLabelsResource getParent() {
        return parent;
    }

    @Override
    public Label get() {
        Labels labels = parent.list();
        if (labels != null) {
            for (Label label : labels.getLabels()) {
                if (label.getId().equals(id)) {
                    label.setNetwork(new Network());
                    label.getNetwork().setId(parent.getNetworkId().toString());
                    return addLinks(label);
                }
            }
        }

        return notFound();
    }

    @Override
    protected Label doPopulate(Label model, NetworkLabel entity) {
        return model;
    }

    @Override
    public Response remove() {
        get();
        return performAction(VdcActionType.UnlabelNetwork, new UnlabelNetworkParameters(parent.getNetworkId()));
    }
}
