package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkLabel;
import org.ovirt.engine.api.model.NetworkLabels;
import org.ovirt.engine.api.resource.NetworkLabelResource;
import org.ovirt.engine.api.resource.NetworkLabelsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LabelNetworkParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendNetworkLabelsResource
    extends AbstractBackendCollectionResource<NetworkLabel, org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel>
    implements NetworkLabelsResource {

    private Guid networkId;

    protected BackendNetworkLabelsResource(Guid networkId) {
        super(NetworkLabel.class, org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel.class);
        this.networkId = networkId;
    }

    @Override
    public NetworkLabels list() {
        return mapCollection(getNetworkLabels(networkId));
    }

    private List<org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel> getNetworkLabels(Guid networkId) {
        return getBackendCollection(QueryType.GetNetworkLabelsByNetworkId, new IdQueryParameters(networkId));
    }

    private NetworkLabels mapCollection(List<org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel> networkLabels) {
        NetworkLabels labels = new NetworkLabels();
        for (org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel networkLabel : networkLabels) {
            NetworkLabel label = new NetworkLabel();
            label.setId(networkLabel.getId());
            labels.getNetworkLabels().add(label);
            addLinks(label, Network.class);
        }

        return labels;
    }

    @Override
    public Response add(NetworkLabel label) {
        validateParameters(label, "id");
        return performCreate(ActionType.LabelNetwork,
                new LabelNetworkParameters(networkId, label.getId()),
                new NetworkLabelIdResolver(networkId));
    }

    @Override
    public NetworkLabelResource getLabelResource(String id) {
        return inject(new BackendNetworkLabelResource(id, this));
    }

    @Override
    protected NetworkLabel addParents(NetworkLabel model) {
        model.setNetwork(new Network());
        model.getNetwork().setId(networkId.toString());
        return model;
    };

    @Override
    protected NetworkLabel addLinks(NetworkLabel model,
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
        public org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel lookupEntity(String id) throws BackendFailureException {
            List<org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel> labels = getNetworkLabels(networkId);
            if (!labels.isEmpty()) {
                return labels.get(0);
            }

            return null;
        }
    }
}
