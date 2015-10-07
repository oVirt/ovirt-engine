package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByUserGroupIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendGroupTagsResource extends AbstractBackendAssignedTagsResource {
    public BackendGroupTagsResource(String parentId) {
        super(Group.class, parentId, VdcActionType.AttachUserGroupToTag);
    }

    public List<Tags> getCollection() {
        return getBackendCollection(VdcQueryType.GetTagsByUserGroupId, new GetTagsByUserGroupIdParameters(parentId));
    }

    @Override
    public AssignedTagResource getTagResource(String id) {
        return inject(new BackendGroupTagResource(asGuid(parentId), id));
    }
}
