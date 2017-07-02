package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.queries.GetTagsByTemplateIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendTemplateTagsResourceTest extends AbstractBackendAssignedTagsResourceTest<BackendTemplateTagsResource> {
    public BackendTemplateTagsResourceTest() {
        super(new BackendTemplateTagsResource(PARENT_GUID.toString()));
        parentIdName = "TemplateId";
        queryType = QueryType.GetTagsByTemplateId;
        queryParams = GetTagsByTemplateIdParameters.class;
        attachAction = ActionType.AttachTemplatesToTag;
        attachParams = AttachEntityToTagParameters.class;
    }
}
