package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.action.AttachVdsToTagParameters;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetTagsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendHostTagsResource
    extends AbstractBackendAssignedTagsResource
    implements AssignedTagsResource {

    public BackendHostTagsResource(String parentId) {
        super(Host.class, parentId, VdcActionType.AttachVdsToTag, VdcActionType.DetachVdsFromTag);
    }

    public List<tags> getCollection() {
        return getBackendCollection(VdcQueryType.GetTagsByVdsId, new GetTagsByVdsIdParameters(parentId));
    }

    protected TagsActionParametersBase getAttachParams(String id) {
        return new AttachVdsToTagParameters(asGuid(id), asList(asGuid(parentId)));
    }

    @Override
    protected Tag doPopulate(Tag model, tags entity) {
        return model;
    }
}
