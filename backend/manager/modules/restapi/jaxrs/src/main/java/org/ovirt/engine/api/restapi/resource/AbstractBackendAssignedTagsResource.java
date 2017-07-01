package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.utils.ReflectionHelper.assignChildModel;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.resource.AssignedTagsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachEntityToTagParameters;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class AbstractBackendAssignedTagsResource
    extends AbstractBackendCollectionResource<Tag, Tags>
    implements AssignedTagsResource {

    protected Class<? extends BaseResource> parentType;
    protected String parentId;
    protected ActionType attachAction;

    public AbstractBackendAssignedTagsResource(Class<? extends BaseResource> parentType,
                                               String parentId,
                                               ActionType attachAction) {
        super(Tag.class, Tags.class);
        this.parentType = parentType;
        this.parentId = parentId;
        this.attachAction = attachAction;
    }

    public String getParentId() {
        return parentId;
    }

    protected abstract List<Tags> getCollection();

    private TagsActionParametersBase getAttachParams(String id) {
        return new AttachEntityToTagParameters(asGuid(id), asList(asGuid(parentId)));
    }

    public org.ovirt.engine.api.model.Tags list() {
        org.ovirt.engine.api.model.Tags ret = new org.ovirt.engine.api.model.Tags();
        for (Tags tag : getCollection()) {
            ret.getTags().add(addLinks(populate(map(tag), tag)));
        }
        return ret;
    }

    public Response add(Tag tag) {
        validateParameters(tag, "id|name");

        if (!tag.isSetId()) {
            tag = lookupTagByName(tag.getName());
        }

        return performCreate(attachAction, getAttachParams(tag.getId()), new TagIdResolver(asGuid(tag.getId())));
    }

    @Override
    public Tag addParents(Tag tag) {
        assignChildModel(tag, parentType).setId(parentId);
        return tag;
    }

    protected Tag lookupTagByName(String name) {
        for (Tags tag : getBackendCollection(Tags.class, QueryType.GetAllTags, new QueryParametersBase())) {
            if (tag.getTagName().equals(name)) {
                return map(tag);
            }
        }
        return handleError(new EntityNotFoundException(name), false);
    }

    public Tags lookupTagById(Guid id) {
        return getEntity(Tags.class, QueryType.GetTagByTagId, new IdQueryParameters(id), id.toString(), true);
    }

    protected class TagIdResolver extends EntityIdResolver<Guid> {

        private Guid id;

        TagIdResolver(Guid id) {
            this.id = id;
        }

        @Override
        public Tags lookupEntity(Guid id) throws BackendFailureException {
            assert id == null; // attach actions return nothing, lookup original id instead
            return lookupTagById(this.id);
        }
    }
}
