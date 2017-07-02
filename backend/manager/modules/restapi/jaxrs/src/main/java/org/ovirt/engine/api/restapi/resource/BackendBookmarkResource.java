package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Bookmark;
import org.ovirt.engine.api.resource.BookmarkResource;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.action.BookmarksParametersBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

public class BackendBookmarkResource extends AbstractBackendSubResource<Bookmark,
    org.ovirt.engine.core.common.businessentities.Bookmark> implements BookmarkResource {

    protected BackendBookmarkResource(String id) {
        super(id, Bookmark.class, org.ovirt.engine.core.common.businessentities.Bookmark.class);
    }

    @Override
    public Bookmark get() {
        return performGet(QueryType.GetBookmarkByBookmarkId, new IdQueryParameters(guid));
    }

    @Override
    public Bookmark update(Bookmark incoming) {
        return performUpdate(incoming, new QueryIdResolver<>(QueryType.GetBookmarkByBookmarkId,
                IdQueryParameters.class), ActionType.UpdateBookmark, new UpdateParametersProvider());
    }

    protected class UpdateParametersProvider implements ParametersProvider<Bookmark,
        org.ovirt.engine.core.common.businessentities.Bookmark> {

        @Override
        public ActionParametersBase getParameters(Bookmark incoming,
                org.ovirt.engine.core.common.businessentities.Bookmark entity) {
            return new BookmarksOperationParameters(map(incoming, entity));
        }
    }

    @Override
    public Response remove() {
        get();
        return performAction(ActionType.RemoveBookmark, new BookmarksParametersBase(guid));
    }
}
