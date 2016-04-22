package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendAffinityLabelsResource.SUB_COLLECTIONS;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.AffinityLabel;
import org.ovirt.engine.api.resource.AssignedAffinityLabelResource;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendAssignedAffinityLabelResource extends AbstractBackendActionableResource<AffinityLabel, org.ovirt.engine.core.common.businessentities.Label>
        implements AssignedAffinityLabelResource {

    private final String parentId;
    private final BackendAssignedAffinityLabelsResource.ReferencedEntityConstructor<BusinessEntity<Guid>> constructor;

    public BackendAssignedAffinityLabelResource(String parentId, BackendAssignedAffinityLabelsResource.ReferencedEntityConstructor<BusinessEntity<Guid>> constructor, String id) {
        super(id, AffinityLabel.class, org.ovirt.engine.core.common.businessentities.Label.class, SUB_COLLECTIONS);
        this.parentId = parentId;
        this.constructor = constructor;
    }

    @Override
    public AffinityLabel get() {
        return addLinks(performGet(VdcQueryType.GetLabelById, new IdQueryParameters(guid)));
    }

    @Override
    public Response remove() {
        QueryIdResolver<Guid> labelResolver = new QueryIdResolver<>(VdcQueryType.GetLabelById, IdQueryParameters.class);
        org.ovirt.engine.core.common.businessentities.Label entity = getEntity(labelResolver, true);

        BusinessEntity<Guid> parent = constructor.create();
        parent.setId(GuidUtils.asGuid(parentId));

        org.ovirt.engine.core.common.businessentities.Label updatedLabel = new LabelBuilder(entity)
                .removeEntity(parent)
                .build();

        return performAction(VdcActionType.UpdateLabel,
                new LabelActionParameters(updatedLabel));
    }
}
