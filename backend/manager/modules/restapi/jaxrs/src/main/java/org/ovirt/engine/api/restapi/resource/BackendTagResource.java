package org.ovirt.engine.api.restapi.resource;


import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.resource.TagResource;
import org.ovirt.engine.core.common.action.MoveTagParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.queries.GetTagByTagIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTagResource
    extends AbstractBackendSubResource<Tag, tags>
    implements TagResource {

    private BackendTagsResource parent;

    public BackendTagResource(String id, BackendTagsResource parent) {
        super(id, Tag.class, tags.class);
        this.parent = parent;
    }

    BackendTagsResource getParent() {
        return parent;
    }

    @Override
    public Tag get() {
        return performGet(VdcQueryType.GetTagByTagId, new GetTagByTagIdParameters(guid));
    }

    @Override
    public Tag update(Tag incoming) {
        if (parent.isSetParentName(incoming)) {
            incoming.getParent().getTag().setId(parent.getParentId(incoming));
        }

        Tag existingTag = get();
        String existingTagParentId =
                existingTag.isSetParent() && existingTag.getParent().isSetTag() && existingTag.getParent().getTag().isSetId() ? existingTag.getParent()
                        .getTag()
                        .getId() : null;
        if (isSetParent(incoming) && !incoming.getParent().getTag().getId().equals(existingTagParentId)) {
            moveTag(asGuid(incoming.getParent().getTag().getId()));
        }

        return performUpdate(incoming,
                             new QueryIdResolver<Guid>(VdcQueryType.GetTagByTagId, GetTagByTagIdParameters.class),
                             VdcActionType.UpdateTag,
                             new UpdateParametersProvider());
    }

    protected void moveTag(Guid newParentId) {
        performAction(VdcActionType.MoveTag, new MoveTagParameters(guid, newParentId), Void.class);
    }

    protected boolean isSetParent(Tag tag) {
        return tag.isSetParent() && tag.getParent().isSetTag() && tag.getParent().getTag().isSetId();
    }

    protected class UpdateParametersProvider implements ParametersProvider<Tag, tags> {
        @Override
        public VdcActionParametersBase getParameters(Tag incoming, tags entity) {
            return new TagsOperationParameters(map(incoming, entity));
        }
    }

    @Override
    protected Tag doPopulate(Tag model, tags entity) {
        return model;
    }
}
