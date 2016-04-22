package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.BaseResources;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendAffinityLabelledEntitiesResource<M extends BaseResource, MS extends BaseResources, E extends BusinessEntity<Guid>> extends AbstractBackendCollectionResource<M, E> {

    public AbstractBackendAffinityLabelledEntitiesResource(String parentId, Class<M> modelCls, Class<E> entityCls, BackendAssignedAffinityLabelsResource.ReferencedEntityConstructor<E> constructor) {
        super(modelCls, entityCls);
        this.parentId = parentId;
        this.constructor = constructor;
    }

    protected final String parentId;
    protected final BackendAssignedAffinityLabelsResource.ReferencedEntityConstructor<E> constructor;

    public Response add(M entity) {
        validateParameters(entity, "id");

        org.ovirt.engine.core.common.businessentities.Label label = getLabel();

        BusinessEntity<Guid> entityStub = constructor.create();
        entityStub.setId(GuidUtils.asGuid(entity.getId()));

        org.ovirt.engine.core.common.businessentities.Label updatedLabel = new LabelBuilder(label)
                .entity(entityStub)
                .build();

        LabelActionParameters updateParams = new LabelActionParameters(updatedLabel);
        return performAction(VdcActionType.UpdateLabel, updateParams);
    }

    protected Label getLabel() {
        IdQueryParameters parameters = new IdQueryParameters(GuidUtils.asGuid(parentId));
        return getEntity(
                Label.class,
                VdcQueryType.GetLabelById,
                parameters,
                parentId,
                true);
    }

    protected abstract MS mapCollection(List<E> entities);
}
