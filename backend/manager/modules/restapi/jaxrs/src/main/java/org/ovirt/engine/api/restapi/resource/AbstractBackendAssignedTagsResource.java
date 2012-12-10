package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.Tags;
import org.ovirt.engine.api.resource.AssignedTagResource;

import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetTagByTagIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.ovirt.engine.api.common.util.ReflectionHelper.assignChildModel;

public abstract class AbstractBackendAssignedTagsResource
    extends AbstractBackendCollectionResource<Tag, tags> {

    protected Class<? extends BaseResource> parentType;
    protected String parentId;
    protected VdcActionType attachAction;
    protected VdcActionType detachAction;

    public AbstractBackendAssignedTagsResource(Class<? extends BaseResource> parentType,
                                               String parentId,
                                               VdcActionType attachAction,
                                               VdcActionType detachAction) {
        super(Tag.class, tags.class);
        this.parentType = parentType;
        this.parentId = parentId;
        this.attachAction = attachAction;
        this.detachAction = detachAction;
    }

    public String getParentId() {
        return parentId;
    }

    public VdcActionType getAttachAction() {
        return attachAction;
    }

    public VdcActionType getDetachAction() {
        return detachAction;
    }

    protected abstract List<tags> getCollection();

    protected abstract TagsActionParametersBase getAttachParams(String id);

    public Tags list() {
        Tags ret = new Tags();
        for (tags tag : getCollection()) {
            ret.getTags().add(addLinks(populate(map(tag), tag)));
        }
        return ret;
    }

    public Response add(Tag tag) {
        validateParameters(tag, "id|name");

        if (!tag.isSetId()) {
            tag = lookupTagByName(tag.getName());
        }

        return performCreation(attachAction,
                               getAttachParams(tag.getId()),
                               new TagIdResolver(asGuid(tag.getId())));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(detachAction, getAttachParams(id));
    }

    @SingleEntityResource
    public AssignedTagResource getAssignedTagSubResource(String id) {
        return inject(new BackendAssignedTagResource(id, this));
    }

    @Override
    public Tag addParents(Tag tag) {
        assignChildModel(tag, parentType).setId(parentId);
        return tag;
    }

    protected Tag lookupTagByName(String name) {
        for (tags tag : getBackendCollection(tags.class, VdcQueryType.GetAllTags, new VdcQueryParametersBase())) {
            if (tag.gettag_name().equals(name)) {
                return map(tag);
            }
        }
        return handleError(new EntityNotFoundException(name), false);
    }

    public tags lookupTagById(Guid id) {
        return getEntity(tags.class, VdcQueryType.GetTagByTagId, new GetTagByTagIdParameters(id), id.toString(), true);
    }

    protected class TagIdResolver extends EntityIdResolver<Guid> {

        private Guid id;

        TagIdResolver(Guid id) {
            this.id = id;
        }

        @Override
        public tags lookupEntity(Guid id) throws BackendFailureException {
            assert (id == null); // attach actions return nothing, lookup original id instead
            return lookupTagById(this.id);
        }
    }
}
