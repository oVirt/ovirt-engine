package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByUserIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendUserTagsResource
    extends AbstractBackendAssignedTagsResource
    implements AssignedTagsResource {

    public BackendUserTagsResource(String parentId) {
        super(User.class, parentId, VdcActionType.AttachUserToTag, VdcActionType.DetachUserFromTag);
    }

    public List<Tags> getCollection() {
        return getBackendCollection(VdcQueryType.GetTagsByUserId, new GetTagsByUserIdParameters(parentId));
    }

    protected TagsActionParametersBase getAttachParams(String id) {
        return new AttachEntityToTagParameters(asGuid(id), asList(asGuid(parentId)));
    }

    @Override
    protected Tag doPopulate(Tag model, Tags entity) {
        return model;
    }
}
