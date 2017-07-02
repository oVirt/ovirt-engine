package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.resource.TagResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MoveTagParameters;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTagResource
    extends AbstractBackendSubResource<Tag, Tags>
    implements TagResource {

    private BackendTagsResource parent;

    public BackendTagResource(String id, BackendTagsResource parent) {
        super(id, Tag.class, Tags.class);
        this.parent = parent;
    }

    BackendTagsResource getParent() {
        return parent;
    }

    @Override
    public Tag get() {
        return performGet(QueryType.GetTagByTagId, new IdQueryParameters(guid));
    }

    @Override
    public Tag update(Tag incoming) {
        if (parent.isSetParentName(incoming)) {
            incoming.getParent().setId(parent.getParentId(incoming));
        }

        Tag existingTag = get();
        String existingTagParentId = existingTag.isSetParent()? existingTag.getParent().getId(): null;
        if (isSetParent(incoming) && !incoming.getParent().getId().equals(existingTagParentId)) {
            moveTag(asGuid(incoming.getParent().getId()));
        }

        return performUpdate(incoming,
                             new QueryIdResolver<>(QueryType.GetTagByTagId, IdQueryParameters.class),
                             ActionType.UpdateTag,
                             new UpdateParametersProvider());
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveTag, new TagsActionParametersBase(guid));
    }

    protected void moveTag(Guid newParentId) {
        performAction(ActionType.MoveTag, new MoveTagParameters(guid, newParentId), Void.class);
    }

    protected boolean isSetParent(Tag tag) {
        return tag.isSetParent() && tag.getParent().isSetId();
    }

    protected class UpdateParametersProvider implements ParametersProvider<Tag, Tags> {
        @Override
        public ActionParametersBase getParameters(Tag incoming, Tags entity) {
            return new TagsOperationParameters(map(incoming, entity));
        }
    }
}
