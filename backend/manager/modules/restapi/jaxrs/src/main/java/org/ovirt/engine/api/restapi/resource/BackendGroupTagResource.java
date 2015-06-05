package org.ovirt.engine.api.restapi.resource;

import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.resource.AssignedTagResource;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.GetTagsByUserGroupIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import javax.ws.rs.core.Response;
import java.util.List;

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
            VdcQueryType.GetTagsByUserGroupId,
            new GetTagsByUserGroupIdParameters(groupId.toString())
        );
        for (Tags tag : tags) {
            if (tag.gettag_id().equals(guid)) {
                return addLinks(populate(map(tag, null), tag));
            }
        }
        return notFound();
    }

    @Override
    protected Tag doPopulate(Tag model, Tags entity) {
        return model;
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
            VdcActionType.DetachUserGroupFromTag,
            new AttachEntityToTagParameters(guid, asList(groupId))
        );
    }
}
