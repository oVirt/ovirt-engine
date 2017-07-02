package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByUserIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendUserTagsResource extends AbstractBackendAssignedTagsResource {
    public BackendUserTagsResource(String parentId) {
        super(User.class, parentId, ActionType.AttachUserToTag);
    }

    public List<Tags> getCollection() {
        return getBackendCollection(QueryType.GetTagsByUserId, new GetTagsByUserIdParameters(parentId));
    }

    @Override
    public AssignedTagResource getTagResource(String id) {
        return inject(new BackendUserTagResource(asGuid(parentId), id));
    }
}
