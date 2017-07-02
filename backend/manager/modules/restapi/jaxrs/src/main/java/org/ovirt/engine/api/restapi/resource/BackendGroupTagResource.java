package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByUserGroupIdParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendGroupTagResource extends AbstractBackendSubResource<Tag, Tags> implements AssignedTagResource {
    private Guid groupId;

    public BackendGroupTagResource(Guid groupId, String tagId) {
        super(tagId, Tag.class, Tags.class);
        this.groupId = groupId;
    }

    @Override
    public Tag get() {
        List<Tags> tags = getBackendCollection(
            Tags.class,
            QueryType.GetTagsByUserGroupId,
            new GetTagsByUserGroupIdParameters(groupId.toString())
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
        Group group = new Group();
        group.setId(groupId.toString());
        tag.setGroup(group);
        return tag;
    }

    @Override
    public Response remove() {
        get();
        return performAction(
            ActionType.DetachUserGroupFromTag,
            new AttachEntityToTagParameters(guid, asList(groupId))
        );
    }
}
