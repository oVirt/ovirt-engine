package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.AffinityLabel;
import org.ovirt.engine.api.resource.AffinityLabelHostsResource;
import org.ovirt.engine.api.resource.AffinityLabelResource;
import org.ovirt.engine.api.resource.AffinityLabelVmsResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.action.LabelActionParametersBase;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendAffinityLabelResource extends AbstractBackendActionableResource<AffinityLabel, Label>
        implements AffinityLabelResource {

    public BackendAffinityLabelResource(String id) {
        super(id, AffinityLabel.class, org.ovirt.engine.core.common.businessentities.Label.class);
    }

    @Override
    public AffinityLabel get() {
        return addLinks(performGet(QueryType.GetLabelById, new IdQueryParameters(guid)));
    }

    @Override
    public AffinityLabel update(AffinityLabel incoming) {
        QueryIdResolver<Guid> labelResolver = new QueryIdResolver<>(QueryType.GetLabelById, IdQueryParameters.class);
        Label entity = getEntity(labelResolver, true);
        AffinityLabel label = performUpdate(incoming,
                entity,
                map(entity),
                labelResolver,
                ActionType.UpdateLabel,
                new UpdateParametersProvider());

        return label;
    }

    @Override
    public Response remove() {
        return performAction(ActionType.RemoveLabel, new LabelActionParametersBase(guid));
    }

    protected static class UpdateParametersProvider implements ParametersProvider<AffinityLabel, Label> {
        @Override
        public ActionParametersBase getParameters(AffinityLabel model,
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
