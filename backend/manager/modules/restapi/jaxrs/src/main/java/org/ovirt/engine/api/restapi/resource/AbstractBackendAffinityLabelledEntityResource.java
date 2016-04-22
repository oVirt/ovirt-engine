package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.restapi.utils.GuidUtils;
import org.ovirt.engine.core.common.action.LabelActionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.LabelBuilder;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class AbstractBackendAffinityLabelledEntityResource<M extends BaseResource, E extends BusinessEntity<Guid>> extends AbstractBackendActionableResource<M, E> {

    private final String parentId;
    private final BackendAssignedAffinityLabelsResource.ReferencedEntityConstructor<E> constructor;

    public AbstractBackendAffinityLabelledEntityResource(String parentId,
            BackendAssignedAffinityLabelsResource.ReferencedEntityConstructor<E> constructor, String id,
            Class<M> resourceCls, Class<E> entityCls) {
        super(id, resourceCls, entityCls);
        this.parentId = parentId;
        this.constructor = constructor;
    }

    public Response remove() {
        IdQueryParameters parameters = new IdQueryParameters(GuidUtils.asGuid(parentId));
        org.ovirt.engine.core.common.businessentities.Label label = getEntity(
                org.ovirt.engine.core.common.businessentities.Label.class,
                VdcQueryType.GetLabelById,
                parameters,
                parentId,
                true);

        BusinessEntity<Guid> entity = constructor.create();
        entity.setId(GuidUtils.asGuid(id));

        org.ovirt.engine.core.common.businessentities.Label updatedLabel = new LabelBuilder(label)
                .removeEntity(entity)
                .build();

        return performAction(VdcActionType.UpdateLabel,
                new LabelActionParameters(updatedLabel));
    }


    public M get() {
        M model;

        try {
            model = modelType.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }

        model.setId(id);
        return addLinks(model, true);
    }
}
