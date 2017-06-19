package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByTemplateIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendTemplateTagsResource extends AbstractBackendAssignedTagsResource {

    public BackendTemplateTagsResource(String parentId) {
        super(Template.class, parentId, ActionType.AttachTemplatesToTag);
    }

    public List<Tags> getCollection() {
        return getBackendCollection(VdcQueryType.GetTagsByTemplateId, new GetTagsByTemplateIdParameters(parentId));
    }

    @Override
    public AssignedTagResource getTagResource(String id) {
        return inject(new BackendTemplateTagResource(asGuid(parentId), id));
    }
}
