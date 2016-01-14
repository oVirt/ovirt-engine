package org.ovirt.engine.api.restapi.resource;

import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Bookmark;
import org.ovirt.engine.api.model.Bookmarks;
import org.ovirt.engine.api.resource.BookmarkResource;
import org.ovirt.engine.api.resource.BookmarksResource;
import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendBookmarksResource extends
    AbstractBackendCollectionResource<Bookmark, org.ovirt.engine.core.common.businessentities.Bookmark> implements
    BookmarksResource {

    public BackendBookmarksResource() {
        super(Bookmark.class, org.ovirt.engine.core.common.businessentities.Bookmark.class);
    }

    @Override
    public Bookmarks list() {
        return mapCollection(getBackendCollection(VdcQueryType.GetAllBookmarks, new VdcQueryParametersBase()));
    }

    @Override
    public Response add(Bookmark bookmark) {
        validateParameters(bookmark, "name");
        validateParameters(bookmark, "value");
        return performCreate(VdcActionType.AddBookmark, new BookmarksOperationParameters(map(bookmark)),
                new BookmarkNameResolver(bookmark.getName()));
    }

    @Override
    public BookmarkResource getBookmarkResource(String id) {
        return inject(new BackendBookmarkResource(id));
    }

    protected Bookmarks mapCollection(List<org.ovirt.engine.core.common.businessentities.Bookmark> entities) {
        Bookmarks collection = new Bookmarks();
        for (org.ovirt.engine.core.common.businessentities.Bookmark entity : entities) {
            collection.getBookmarks().add(addLinks(map(entity)));
        }
        return collection;
    }

    protected org.ovirt.engine.core.common.businessentities.Bookmark lookupBookmarkByName(final String name) {
        return getEntity(org.ovirt.engine.core.common.businessentities.Bookmark.class,
                VdcQueryType.GetBookmarkByBookmarkName, new NameQueryParameters(name), name);
    }

    protected class BookmarkNameResolver extends EntityIdResolver<Guid> {

        private final String name;

        BookmarkNameResolver(String name) {
            this.name = name;
        }

        @Override
        public org.ovirt.engine.core.common.businessentities.Bookmark lookupEntity(Guid id) throws
            BackendFailureException {
            assert id == null; // AddBookmark returns nothing, lookup name instead
            return lookupBookmarkByName(name);
        }
    }

}
