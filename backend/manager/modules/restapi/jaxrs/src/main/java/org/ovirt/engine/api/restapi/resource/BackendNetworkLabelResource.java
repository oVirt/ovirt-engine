package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkLabel;
import org.ovirt.engine.api.model.NetworkLabels;
import org.ovirt.engine.api.resource.NetworkLabelResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.UnlabelNetworkParameters;

public class BackendNetworkLabelResource
    extends AbstractBackendSubResource<NetworkLabel, org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel>
    implements NetworkLabelResource {

    private BackendNetworkLabelsResource parent;

    protected BackendNetworkLabelResource(String id, BackendNetworkLabelsResource parent) {
        super("", NetworkLabel.class, org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel.class);
        this.id = id;
        this.parent = parent;
    }

    public BackendNetworkLabelsResource getParent() {
        return parent;
    }

    @Override
    public NetworkLabel get() {
        NetworkLabels labels = parent.list();
        if (labels != null) {
            for (NetworkLabel label : labels.getNetworkLabels()) {
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
    public Response remove() {
        get();
        return performAction(ActionType.UnlabelNetwork, new UnlabelNetworkParameters(parent.getNetworkId()));
    }
}
