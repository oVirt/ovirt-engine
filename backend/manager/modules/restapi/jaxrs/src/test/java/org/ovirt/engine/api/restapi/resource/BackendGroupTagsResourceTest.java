package org.ovirt.engine.api.restapi.resource;

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.queries.GetTagsByUserGroupIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendGroupTagsResourceTest extends AbstractBackendAssignedTagsResourceTest<BackendGroupTagsResource> {
    public BackendGroupTagsResourceTest() {
        super(new BackendGroupTagsResource(PARENT_GUID.toString()));
        parentIdName = "GroupId";
        queryType = QueryType.GetTagsByUserGroupId;
        queryParams = GetTagsByUserGroupIdParameters.class;
        attachAction = ActionType.AttachUserGroupToTag;
        attachParams = AttachEntityToTagParameters.class;
    }
}
