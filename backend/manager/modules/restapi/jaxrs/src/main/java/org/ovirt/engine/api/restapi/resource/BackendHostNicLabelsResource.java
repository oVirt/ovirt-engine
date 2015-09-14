package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.Label;
import org.ovirt.engine.api.model.Labels;
import org.ovirt.engine.api.resource.LabelResource;
import org.ovirt.engine.api.resource.LabelsResource;
import org.ovirt.engine.core.common.action.LabelNicParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendHostNicLabelsResource extends AbstractBackendCollectionResource<Label, NetworkLabel> implements LabelsResource {

    private Guid nicId;
    private String hostId;

    protected BackendHostNicLabelsResource(Guid nicId, String hostId) {
        super(Label.class, NetworkLabel.class);
        this.nicId = nicId;
        this.hostId = hostId;
    }

    @Override
    public Labels list() {
        return mapCollection(getHostNicLabels(nicId));
    }

    private List<NetworkLabel> getHostNicLabels(Guid hostNicId) {
        return getBackendCollection(VdcQueryType.GetNetworkLabelsByHostNicId, new IdQueryParameters(hostNicId));
    }

    private Labels mapCollection(List<NetworkLabel> networkLabels) {
        Labels labels = new Labels();
        for (NetworkLabel networkLabel : networkLabels) {
            Label label = new Label();
            label.setId(networkLabel.getId());
            labels.getLabels().add(label);
            addLinks(label, HostNic.class);
        }

        return labels;
    }

    @Override
    public Response add(Label label) {
        validateParameters(label, "id");
        return performCreate(VdcActionType.LabelNic,
                new LabelNicParameters(nicId, label.getId()),
                new NetworkLabelIdResolver(nicId, label.getId()));
    }

    @Override
    public LabelResource getLabelSubResource(String id) {
        return inject(new BackendHostNicLabelResource(id, this));
    }

    @Override
    protected Label addParents(Label model) {
        model.setHostNic(new HostNic());
        model.getHostNic().setId(nicId.toString());
        model.getHostNic().setHost(new Host());
        model.getHostNic().getHost().setId(hostId);
        return model;
    }

    @Override
    protected Label addLinks(Label model,
            Class<? extends BaseResource> suggestedParent,
            String... excludeSubCollectionMembers) {
        return super.addLinks(model, HostNic.class);
    }

    public Guid getHostNicId() {
        return nicId;
    }

    protected class NetworkLabelIdResolver extends EntityIdResolver<String> {

        private Guid nicId;

        NetworkLabelIdResolver() {
        }

        NetworkLabelIdResolver(Guid nicId, String label) {
            this.nicId = nicId;
        }

        @Override
        public NetworkLabel lookupEntity(String id) throws BackendFailureException {
            List<NetworkLabel> labels = getHostNicLabels(nicId);
            for (NetworkLabel label : labels) {
                if (Objects.equals(label.getId(), id)) {
                    return label;
                }
            }

            return null;
        }
    }
}
