package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.queries.GetTagsByVmIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendVmTagsResourceTest extends AbstractBackendAssignedTagsResourceTest<BackendVmTagsResource> {
    public BackendVmTagsResourceTest() {
        super(new BackendVmTagsResource(PARENT_GUID.toString()));
        parentIdName = "VmId";
        queryType = VdcQueryType.GetTagsByVmId;
        queryParams = GetTagsByVmIdParameters.class;
        attachAction = ActionType.AttachVmsToTag;
        attachParams = AttachEntityToTagParameters.class;
    }
}
