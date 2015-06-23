package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Label;
import org.ovirt.engine.api.model.Labels;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.resource.LabelResource;
import org.ovirt.engine.api.resource.LabelsResource;
import org.ovirt.engine.core.common.action.LabelNetworkParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendNetworkLabelsResource extends AbstractBackendCollectionResource<Label, NetworkLabel> implements LabelsResource {

    private Guid networkId;

    protected BackendNetworkLabelsResource(Guid networkId) {
        super(Label.class, NetworkLabel.class);
        this.networkId = networkId;
    }

    @Override
    public Labels list() {
        return mapCollection(getNetworkLabels(networkId));
    }

    private List<NetworkLabel> getNetworkLabels(Guid networkId) {
        return getBackendCollection(VdcQueryType.GetNetworkLabelsByNetworkId, new IdQueryParameters(networkId));
    }

    private Labels mapCollection(List<NetworkLabel> networkLabels) {
        Labels labels = new Labels();
        for (NetworkLabel networkLabel : networkLabels) {
            Label label = new Label();
            label.setId(networkLabel.getId());
            labels.getLabels().add(label);
            addLinks(label, Network.class);
        }

        return labels;
    }

    @Override
    public Response add(Label label) {
        validateParameters(label, "id");
        return performCreate(VdcActionType.LabelNetwork,
                new LabelNetworkParameters(networkId, label.getId()),
                new NetworkLabelIdResolver(networkId));
    }

    @SingleEntityResource
    @Override
    public LabelResource getLabelSubResource(String id) {
        return inject(new BackendNetworkLabelResource(id, this));
    }

    @Override
    protected Label addParents(Label model) {
        model.setNetwork(new Network());
        model.getNetwork().setId(networkId.toString());
        return model;
    };

    @Override
    protected Label addLinks(Label model,
            Class<? extends BaseResource> suggestedParent,
            String... excludeSubCollectionMembers) {
        return super.addLinks(model, Network.class);
    }

    public Guid getNetworkId() {
        return networkId;
    }

    protected class NetworkLabelIdResolver extends EntityIdResolver<String> {

        private Guid networkId;

        NetworkLabelIdResolver() {
        }

        NetworkLabelIdResolver(Guid networkId) {
            this.networkId = networkId;
        }

        @Override
        public NetworkLabel lookupEntity(String id) throws BackendFailureException {
            List<NetworkLabel> labels = getNetworkLabels(networkId);
            if (!labels.isEmpty()) {
                return labels.get(0);
            }

            return null;
        }
    }
}
