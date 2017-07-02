package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Host;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByVdsIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendHostTagsResource extends AbstractBackendAssignedTagsResource {
    public BackendHostTagsResource(String parentId) {
        super(Host.class, parentId, ActionType.AttachVdsToTag);
    }

    public List<Tags> getCollection() {
        return getBackendCollection(QueryType.GetTagsByVdsId, new GetTagsByVdsIdParameters(parentId));
    }

    @Override
    public AssignedTagResource getTagResource(String id) {
        return inject(new BackendHostTagResource(asGuid(parentId), id));
    }
}
