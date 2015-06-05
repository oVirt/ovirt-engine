package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.queries.GetTagsByUserGroupIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.VdcActionType;

public class BackendGroupTagsResourceTest extends AbstractBackendAssignedTagsResourceTest<BackendGroupTagsResource> {
    public BackendGroupTagsResourceTest() {
        super(new BackendGroupTagsResource(PARENT_GUID.toString()));
        parentIdName = "GroupId";
        queryType = VdcQueryType.GetTagsByUserGroupId;
        queryParams = GetTagsByUserGroupIdParameters.class;
        attachAction = VdcActionType.AttachUserGroupToTag;
        attachParams = AttachEntityToTagParameters.class;
    }
}
