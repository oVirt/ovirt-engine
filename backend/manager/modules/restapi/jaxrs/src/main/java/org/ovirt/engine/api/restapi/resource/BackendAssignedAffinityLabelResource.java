package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.AffinityLabel;
import org.ovirt.engine.api.resource.AssignedAffinityLabelResource;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendAssignedAffinityLabelResource extends AbstractBackendActionableResource<AffinityLabel, org.ovirt.engine.core.common.businessentities.Label>
        implements AssignedAffinityLabelResource {

    private final String parentId;
    private final BackendAssignedAffinityLabelsResource.ReferencedEntityConstructor<BusinessEntity<Guid>> constructor;
    private BackendAssignedAffinityLabelsResource parent;

    public BackendAssignedAffinityLabelResource(BackendAssignedAffinityLabelsResource parent, String parentId, BackendAssignedAffinityLabelsResource.ReferencedEntityConstructor<BusinessEntity<Guid>> constructor, String id) {
        super(id, AffinityLabel.class, org.ovirt.engine.core.common.businessentities.Label.class);
        this.parentId = parentId;
        this.parent = parent;
        this.constructor = constructor;
    }

    @Override
    public AffinityLabel get() {
        AffinityLabel affinityLabel = addLinks(performGet(QueryType.GetLabelById, new IdQueryParameters(guid)));
        parent.linkSubCollections(affinityLabel);
        return affinityLabel;
    }

    @Override
    public Response remove() {
        QueryIdResolver<Guid> labelResolver = new QueryIdResolver<>(QueryType.GetLabelById, IdQueryParameters.class);
        org.ovirt.engine.core.common.businessentities.Label entity = getEntity(labelResolver, true);

        BusinessEntity<Guid> parent = constructor.create();
        parent.setId(GuidUtils.asGuid(parentId));

        org.ovirt.engine.core.common.businessentities.Label updatedLabel = new LabelBuilder(entity)
                .removeEntity(parent)
                .build();

        return performAction(ActionType.UpdateLabel,
                new LabelActionParameters(updatedLabel));
    }
}
