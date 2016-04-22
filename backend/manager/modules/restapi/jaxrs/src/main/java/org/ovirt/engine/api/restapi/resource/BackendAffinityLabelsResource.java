package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.AffinityLabel;
import org.ovirt.engine.api.model.AffinityLabels;
import org.ovirt.engine.api.resource.AffinityLabelResource;
import org.ovirt.engine.api.resource.AffinityLabelsResource;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityLabelsResource extends AbstractBackendCollectionResource<AffinityLabel, org.ovirt.engine.core.common.businessentities.Label>
        implements AffinityLabelsResource {
    static final String[] SUB_COLLECTIONS = { "hosts", "vms" };

    public BackendAffinityLabelsResource() {
        super(AffinityLabel.class, org.ovirt.engine.core.common.businessentities.Label.class, SUB_COLLECTIONS);
    }

    @Override
    public Response add(AffinityLabel label) {
        validateParameters(label, "name");

        org.ovirt.engine.core.common.businessentities.Label newLabel = map(label);
        LabelActionParameters params = new LabelActionParameters(newLabel);

        return performCreate(VdcActionType.AddLabel, params,
                new QueryIdResolver<Guid>(VdcQueryType.GetLabelById, IdQueryParameters.class));
    }

    @Override
    public AffinityLabels list() {
        return mapCollection(
                getBackendCollection(
                        VdcQueryType.GetAllLabels, new VdcQueryParametersBase()));
    }

    @Override
    public AffinityLabelResource getLabelResource(@PathParam("id") String id) {
        return inject(new BackendAffinityLabelResource(id));
    }

    private AffinityLabels mapCollection(List<org.ovirt.engine.core.common.businessentities.Label> entities) {
        AffinityLabels collection = new AffinityLabels();
        for (org.ovirt.engine.core.common.businessentities.Label entity : entities) {
            collection.getAffinityLabels().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }
}
