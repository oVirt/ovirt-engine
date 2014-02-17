package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByTemplateIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

import java.util.List;

public class BackendTemplateTagsResource
    extends AbstractBackendAssignedTagsResource
    implements AssignedTagsResource {

    public BackendTemplateTagsResource(String parentId) {
        super(Template.class, parentId, VdcActionType.AttachTemplatesToTag, VdcActionType.DetachTemplateFromTag);
    }

    public List<Tags> getCollection() {
        return getBackendCollection(VdcQueryType.GetTagsByTemplateId, new GetTagsByTemplateIdParameters(parentId));
    }

    protected TagsActionParametersBase getAttachParams(String id) {
        return new AttachEntityToTagParameters(asGuid(id), asList(asGuid(parentId)));
    }

    @Override
    protected Tag doPopulate(Tag model, Tags entity) {
        return model;
    }
}
