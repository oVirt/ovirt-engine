package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.resource.TagResource;
import org.ovirt.engine.api.resource.TagsResource;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTagsResource
    extends AbstractBackendCollectionResource<Tag, Tags>
    implements TagsResource {

    public BackendTagsResource() {
        super(Tag.class, Tags.class);
    }

    @Override
    public org.ovirt.engine.api.model.Tags list() {
        List<Tags> tags = getTags();
        tags.add(getRootTag());
        return mapCollection(tags);
    }

    @Override
    public TagResource getTagResource(String id) {
        return inject(new BackendTagResource(id, this));
    }

    @Override
    public Response add(Tag tag) {
        validateParameters(tag, "name");

        if (isSetParentName(tag)) {
            tag.getParent().setId(getParentId(tag));
        }

        return performCreate(ActionType.AddTag,
                               new TagsOperationParameters(map(tag)),
                               new TagNameResolver(tag.getName()));
    }

    protected List<Tags> getTags() {
        return getBackendCollection(QueryType.GetAllTags, new QueryParametersBase());
    }

    protected Tags getRootTag() {
        return getEntity(Tags.class, QueryType.GetRootTag, new QueryParametersBase(), "root");
    }

    protected org.ovirt.engine.api.model.Tags mapCollection(List<Tags> entities) {
        org.ovirt.engine.api.model.Tags collection = new org.ovirt.engine.api.model.Tags();
        for (Tags entity : entities) {
            collection.getTags().add(addLinks(map(entity)));
        }
        return collection;
    }

    boolean isSetParentName(Tag tag) {
        return tag.isSetParent() && tag.getParent().isSetName();
    }

    String getParentId(Tag tag) {
        return lookupTagByName(tag.getParent().getName()).getTagId().toString();
    }

    protected Tags lookupTagByName(String name) {
        return getEntity(Tags.class, QueryType.GetTagByTagName, new NameQueryParameters(name), name);
    }

    protected class TagNameResolver extends EntityIdResolver<Guid> {

        private String name;

        TagNameResolver(String name) {
            this.name = name;
        }

        @Override
        public Tags lookupEntity(Guid id) throws BackendFailureException {
            assert id == null; // AddTag returns nothing, lookup name instead
            return lookupTagByName(name);
        }
    }
}
