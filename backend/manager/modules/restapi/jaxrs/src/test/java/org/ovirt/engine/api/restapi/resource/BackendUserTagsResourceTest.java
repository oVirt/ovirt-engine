package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.queries.GetTagsByUserIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendUserTagsResourceTest extends AbstractBackendAssignedTagsResourceTest<BackendUserTagsResource> {
    public BackendUserTagsResourceTest() {
        super(new BackendUserTagsResource(PARENT_GUID.toString()));
        parentIdName = "UserId";
        queryType = QueryType.GetTagsByUserId;
        queryParams = GetTagsByUserIdParameters.class;
        attachAction = ActionType.AttachUserToTag;
        attachParams = AttachEntityToTagParameters.class;
    }
}
