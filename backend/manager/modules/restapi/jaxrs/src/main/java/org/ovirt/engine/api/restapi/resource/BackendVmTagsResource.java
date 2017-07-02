package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Vm;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByVmIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendVmTagsResource extends AbstractBackendAssignedTagsResource {
    public BackendVmTagsResource(String parentId) {
        super(Vm.class, parentId, ActionType.AttachVmsToTag);
    }

    public List<Tags> getCollection() {
        return getBackendCollection(QueryType.GetTagsByVmId, new GetTagsByVmIdParameters(parentId));
    }

    @Override
    public AssignedTagResource getTagResource(String id) {
        return inject(new BackendVmTagResource(asGuid(parentId), id));
    }
}
