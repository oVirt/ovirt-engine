package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.HostNic;
import org.ovirt.engine.api.model.NetworkLabel;
import org.ovirt.engine.api.model.NetworkLabels;
import org.ovirt.engine.api.resource.NetworkLabelResource;
import org.ovirt.engine.api.resource.NetworkLabelsResource;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBaseHostNicLabelsResource
    extends AbstractBackendCollectionResource<NetworkLabel, org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel>
    implements NetworkLabelsResource {

    private Guid nicId;
    private String hostId;

    protected AbstractBaseHostNicLabelsResource(Guid nicId, String hostId) {
        super(NetworkLabel.class, org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel.class);
        this.nicId = nicId;
        this.hostId = hostId;
    }

    @Override
    public NetworkLabels list() {
        return mapCollection(getHostNicLabels(nicId));
    }

    protected abstract List<org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel> getHostNicLabels(Guid hostNicId);

    private NetworkLabels mapCollection(List<org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel> networkLabels) {
        NetworkLabels labels = new NetworkLabels();
        for (org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel networkLabel : networkLabels) {
            NetworkLabel label = new NetworkLabel();
            label.setId(networkLabel.getId());
            labels.getNetworkLabels().add(label);
            addLinks(label, HostNic.class);
        }

        return labels;
    }

    @Override
    public Response add(NetworkLabel label) {
        validateParameters(label, "id");
        final String labelId = label.getId();
        return performCreate(labelId);
    }

    protected abstract Response performCreate(String labelId);

    @Override
    public NetworkLabelResource getLabelResource(String id) {
        return inject(createSingularResource(id));
    }

    protected abstract AbstractBaseHostNicLabelResource createSingularResource(String labelId);

    @Override
    protected NetworkLabel addParents(NetworkLabel model) {
        model.setHostNic(new HostNic());
        model.getHostNic().setId(nicId.toString());
        model.getHostNic().setHost(new Host());
        model.getHostNic().getHost().setId(hostId);
        return model;
    }

    @Override
    protected NetworkLabel addLinks(NetworkLabel label,
            Class<? extends BaseResource> suggestedParent,
            String... excludeSubCollectionMembers) {
        NetworkLabel resultLabel = super.addLinks(label, HostNic.class);
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
        public org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel lookupEntity(String id) throws BackendFailureException {
            List<org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel> labels = getHostNicLabels(nicId);
            for (org.ovirt.engine.core.common.businessentities.network.pseudo.NetworkLabel label : labels) {
                if (Objects.equals(label.getId(), id)) {
                    return label;
                }
            }

            return null;
        }
    }
}
