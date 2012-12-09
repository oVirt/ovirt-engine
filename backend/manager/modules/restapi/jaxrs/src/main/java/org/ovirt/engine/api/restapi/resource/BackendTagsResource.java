package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;


import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.Tags;
import org.ovirt.engine.api.resource.TagResource;
import org.ovirt.engine.api.resource.TagsResource;

import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.tags;
import org.ovirt.engine.core.common.queries.GetTagByTagNameParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTagsResource
    extends AbstractBackendCollectionResource<Tag, tags>
    implements TagsResource {

    public BackendTagsResource() {
        super(Tag.class, tags.class);
    }

    @Override
    public Tags list() {
        List<tags> tags = getTags();
        tags.add(getRootTag());
        return mapCollection(tags);
    }

    @Override
    @SingleEntityResource
    public TagResource getTagSubResource(String id) {
        return inject(new BackendTagResource(id, this));
    }

    @Override
    public Response add(Tag tag) {
        validateParameters(tag, "name");

        if (isSetParentName(tag)) {
            tag.getParent().getTag().setId(getParentId(tag));
        }

        return performCreate(VdcActionType.AddTag,
                               new TagsOperationParameters(map(tag)),
                               new TagNameResolver(tag.getName()));
    }

    @Override
    public Response performRemove(String id) {
        return performAction(VdcActionType.RemoveTag, new TagsActionParametersBase(asGuid(id)));
    }

    protected List<tags> getTags() {
        return getBackendCollection(VdcQueryType.GetAllTags, new VdcQueryParametersBase());
    }

    protected tags getRootTag() {
        return getEntity(tags.class, VdcQueryType.GetRootTag, new VdcQueryParametersBase(), "root");
    }

    protected Tags mapCollection(List<tags> entities) {
        Tags collection = new Tags();
        for (tags entity : entities) {
            collection.getTags().add(addLinks(map(entity)));
        }
        return collection;
    }

    boolean isSetParentName(Tag tag) {
        return tag.isSetParent() && tag.getParent().isSetTag() && tag.getParent().getTag().isSetName();
    }

    String getParentId(Tag tag) {
        return lookupTagByName(tag.getParent().getTag().getName()).gettag_id().toString();
    }

    protected tags lookupTagByName(String name) {
        return getEntity(tags.class, VdcQueryType.GetTagByTagName, new GetTagByTagNameParameters(name), name);
    }

    protected class TagNameResolver extends EntityIdResolver<Guid> {

        private String name;

        TagNameResolver(String name) {
            this.name = name;
        }

        @Override
        public tags lookupEntity(Guid id) throws BackendFailureException {
            assert (id == null); // AddTag returns nothing, lookup name instead
            return lookupTagByName(name);
        }
    }

    @Override
    protected Tag doPopulate(Tag model, tags entity) {
        return model;
    }
}
