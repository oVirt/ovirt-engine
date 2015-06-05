package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetTagsByTemplateIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendTemplateTagsResourceTest extends AbstractBackendAssignedTagsResourceTest<BackendTemplateTagsResource> {
    public BackendTemplateTagsResourceTest() {
        super(new BackendTemplateTagsResource(PARENT_GUID.toString()));
        parentIdName = "TemplateId";
        queryType = VdcQueryType.GetTagsByTemplateId;
        queryParams = GetTagsByTemplateIdParameters.class;
        attachAction = VdcActionType.AttachTemplatesToTag;
        attachParams = AttachEntityToTagParameters.class;
    }
}
