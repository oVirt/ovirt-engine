package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.ovirt.engine.api.restapi.resource.BackendBookmarksResourceTest.VALUES;
import static org.ovirt.engine.api.restapi.resource.BackendBookmarksResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendBookmarksResourceTest.setUpBookmarks;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Bookmark;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.action.BookmarksParametersBase;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendBookmarkResourceTest extends AbstractBackendSubResourceTest<Bookmark,
    org.ovirt.engine.core.common.businessentities.Bookmark, BackendBookmarkResource> {

    public BackendBookmarkResourceTest() {
        super(new BackendBookmarkResource(GUIDS[0].toString()));
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> new BackendBookmarkResource("foo")));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        //Get will return 404
        setUpGetEntityExpectations(0, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.get()));
    }

    @Test
    public void testGet() {
        setUpGetEntityExpectations(0);
        setUriInfo(setUpBasicUriExpectations());


        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        //Get will return 404
        setUpGetEntityExpectations(0, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))));
    }

    @Test
    public void testUpdate() {
        setUpGetEntityExpectations(0);
        setUpGetEntityExpectations(0);

        setUriInfo(setUpActionExpectations(ActionType.UpdateBookmark, BookmarksOperationParameters.class,
                new String[] { "Bookmark.Id", "Bookmark.Name", "Bookmark.Value" },
                new Object[] { GUIDS[0], NAMES[0], VALUES[0] }, true, true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations(0);


        setUriInfo(setUpActionExpectations(ActionType.UpdateBookmark, BookmarksOperationParameters.class,
                new String[] {}, new Object[] {}, valid, success));

        verifyFault(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))), detail);
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations(0);
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveBookmark,
                BookmarksParametersBase.class,
                new String[] { "BookmarkId" },
                new Object[] { GUIDS[0] },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUpGetEntityExpectations(0, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    private void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations(0);
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveBookmark,
                BookmarksParametersBase.class,
                new String[] { "BookmarkId" },
                new Object[] { GUIDS[0] },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Bookmark getEntity(int index) {
        return setUpBookmarks().get(index);
    }

    @Override
    protected void verifyModel(Bookmark model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertEquals(VALUES[index], model.getValue());
        verifyLinks(model);
    }

    protected void setUpGetEntityExpectations(int index) {
        setUpGetEntityExpectations(index, false);
    }

    protected void setUpGetEntityExpectations(int index, boolean notFound) {
        setUpGetEntityExpectations(QueryType.GetBookmarkByBookmarkId,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[index] },
                                   notFound ? null : getEntity(index));
    }
}
