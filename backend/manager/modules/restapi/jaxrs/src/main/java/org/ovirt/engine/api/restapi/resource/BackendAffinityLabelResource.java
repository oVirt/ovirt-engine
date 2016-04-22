package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendAffinityLabelsResource.SUB_COLLECTIONS;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.AffinityLabel;
import org.ovirt.engine.api.resource.AffinityLabelHostsResource;
import org.ovirt.engine.api.resource.AffinityLabelResource;
import org.ovirt.engine.api.resource.AffinityLabelVmsResource;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.action.LabelActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityLabelResource extends AbstractBackendActionableResource<AffinityLabel, Label>
        implements AffinityLabelResource {

    public BackendAffinityLabelResource(String id) {
        super(id, AffinityLabel.class, org.ovirt.engine.core.common.businessentities.Label.class, SUB_COLLECTIONS);
    }

    @Override
    public AffinityLabel get() {
        return addLinks(performGet(VdcQueryType.GetLabelById, new IdQueryParameters(guid)));
    }

    @Override
    public AffinityLabel update(AffinityLabel incoming) {
        QueryIdResolver<Guid> labelResolver = new QueryIdResolver<>(VdcQueryType.GetLabelById, IdQueryParameters.class);
        Label entity = getEntity(labelResolver, true);
        AffinityLabel label = performUpdate(incoming,
                entity,
                map(entity),
                labelResolver,
                VdcActionType.UpdateLabel,
                new UpdateParametersProvider());

        return label;
    }

    @Override
    public Response remove() {
        return performAction(VdcActionType.RemoveLabel, new LabelActionParametersBase(guid));
    }

    protected static class UpdateParametersProvider implements ParametersProvider<AffinityLabel, Label> {
        @Override
        public VdcActionParametersBase getParameters(AffinityLabel model,
                Label entity) {
            LabelBuilder newEntity = new LabelBuilder(entity);

            if (model.isSetReadOnly()) {
                newEntity.readOnly(model.isReadOnly());
            }

            if (model.isSetName()) {
                newEntity.name(model.getName());
            }

            return new LabelActionParameters(newEntity.build());
        }
    }

    @Override
    public AffinityLabelVmsResource getVmsResource() {
        return inject(new BackendAffinityLabelVmsResource(id.toString()));
    }

    @Override
    public AffinityLabelHostsResource getHostsResource() {
        return inject(new BackendAffinityLabelHostsResource(id.toString()));
    }
}
