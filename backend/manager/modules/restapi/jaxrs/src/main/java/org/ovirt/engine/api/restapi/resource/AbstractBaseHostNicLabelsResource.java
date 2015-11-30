package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostNIC;
import org.ovirt.engine.api.model.Label;
import org.ovirt.engine.api.model.Labels;
import org.ovirt.engine.api.resource.LabelResource;
import org.ovirt.engine.api.resource.LabelsResource;
import org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBaseHostNicLabelsResource extends AbstractBackendCollectionResource<Label, NetworkLabel>
        implements LabelsResource {

    private Guid nicId;
    private String hostId;

    protected AbstractBaseHostNicLabelsResource(Guid nicId, String hostId) {
        super(Label.class, NetworkLabel.class);
        this.nicId = nicId;
        this.hostId = hostId;
    }

    @Override
    public Labels list() {
        return mapCollection(getHostNicLabels(nicId));
    }

    protected abstract List<NetworkLabel> getHostNicLabels(Guid hostNicId);

    private Labels mapCollection(List<NetworkLabel> networkLabels) {
        Labels labels = new Labels();
        for (NetworkLabel networkLabel : networkLabels) {
            Label label = new Label();
            label.setId(networkLabel.getId());
            labels.getLabels().add(label);
            addLinks(label, HostNIC.class);
        }

        return labels;
    }

    @Override
    public Response add(Label label) {
        validateParameters(label, "id");
        final String labelId = label.getId();
        return performCreate(labelId);
    }

    protected abstract Response performCreate(String labelId);

    @Override
    public LabelResource getLabelSubResource(String id) {
        return inject(createSingularResource(id));
    }

    protected abstract AbstractBaseHostNicLabelResource createSingularResource(String labelId);

    @Override
    protected Label addParents(Label model) {
        model.setHostNic(new HostNIC());
        model.getHostNic().setId(nicId.toString());
        model.getHostNic().setHost(new Host());
        model.getHostNic().getHost().setId(hostId);
        return model;
    }

    @Override
    protected Label addLinks(Label label,
            Class<? extends BaseResource> suggestedParent,
            String... excludeSubCollectionMembers) {
        Label resultLabel = super.addLinks(label, HostNIC.class);
        final AbstractBaseHostNicLabelResource labelResource = createSingularResource(resultLabel.getId());
        labelResource.overrideHref(resultLabel);
        return resultLabel;
    }

    public Guid getHostNicId() {
        return nicId;
    }

    protected class NetworkLabelIdResolver extends EntityIdResolver<String> {

        private final Guid nicId;

        NetworkLabelIdResolver(Guid nicId) {
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
