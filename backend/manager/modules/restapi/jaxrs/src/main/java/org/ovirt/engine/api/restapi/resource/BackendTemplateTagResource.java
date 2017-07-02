package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByTemplateIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTemplateTagResource extends AbstractBackendSubResource<Tag, Tags> implements AssignedTagResource {
    private Guid templateId;

    public BackendTemplateTagResource(Guid templateId, String tagId) {
        super(tagId, Tag.class, Tags.class);
        this.templateId = templateId;
    }

    @Override
    public Tag get() {
        List<Tags> tags = getBackendCollection(
            Tags.class,
            QueryType.GetTagsByTemplateId,
            new GetTagsByTemplateIdParameters(templateId.toString())
        );
        for (Tags tag : tags) {
            if (tag.getTagId().equals(guid)) {
                return addLinks(populate(map(tag, null), tag));
            }
        }
        return notFound();
    }

    @Override
    protected Tag addParents(Tag tag) {
        Template template = new Template();
        template.setId(templateId.toString());
        tag.setTemplate(template);
        return tag;
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.DetachTemplateFromTag, new AttachEntityToTagParameters(guid, asList(templateId)));
    }
}
