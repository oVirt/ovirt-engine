package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Bookmark;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendBookmarksResourceTest extends AbstractBackendCollectionResourceTest<Bookmark,
    org.ovirt.engine.core.common.businessentities.Bookmark, BackendBookmarksResource> {

    static final String[] VALUES = {"host.name='blah'", "vms.status='down'", "template.description='something'"};

    public BackendBookmarksResourceTest() {
        super(new BackendBookmarksResource(), null, "");
    }

    @Test
    public void testAddBookmark() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.AddBookmark, BookmarksOperationParameters.class,
                new String[] { "Bookmark.Name", "Bookmark.Value" },
                new Object[] { NAMES[0], VALUES[0] }, true, true, null, QueryType.GetBookmarkByBookmarkName,
                NameQueryParameters.class, new String[] { "Name" }, new Object[] { NAMES[0] }, getEntity(0));

        Response response = collection.add(getModel(0));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Bookmark);
        verifyModel((Bookmark)response.getEntity(), 0);
    }

    @Test
    public void testAddIncompleteParameters() {
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(new Bookmark())),
                "Bookmark", "add", "name");
    }

    @Test
    public void testAddBookmarkCantDo() {
        doTestBadAddBookmark(false, true, CANT_DO);
    }

    @Test
    public void testAddBookmarkFailure() {
        doTestBadAddBookmark(true, false, FAILURE);
    }

    /*************************************************************************************
     * Helpers.
     *************************************************************************************/

    private void doTestBadAddBookmark(boolean valid, boolean success, String detail) {
        setUriInfo(setUpActionExpectations(ActionType.AddBookmark, BookmarksOperationParameters.class,
                new String[] { "Bookmark.Name", "Bookmark.Value" },
                new Object[] { NAMES[0], VALUES[0] }, valid, success));

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(getModel(0))), detail);
    }

    @Override
    protected List<Bookmark> getCollection() {
        return collection.list().getBookmarks();
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Bookmark getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.Bookmark bookmark =
                new org.ovirt.engine.core.common.businessentities.Bookmark();
        bookmark.setId(GUIDS[index]);
        bookmark.setName(NAMES[index]);
        bookmark.setValue(VALUES[index]);
        return bookmark;
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(QueryType.GetAllBookmarks,
                                     QueryParametersBase.class,
                                     new String[] { },
                                     new Object[] { },
                                     setUpBookmarks(),
                                     failure);
    }

    static List<org.ovirt.engine.core.common.businessentities.Bookmark> setUpBookmarks() {
        List<org.ovirt.engine.core.common.businessentities.Bookmark> bookmarks = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            org.ovirt.engine.core.common.businessentities.Bookmark bookmark =
                    new org.ovirt.engine.core.common.businessentities.Bookmark();
            bookmark.setId(GUIDS[i]);
            bookmark.setName(NAMES[i]);
            bookmark.setValue(VALUES[i]);
            bookmarks.add(bookmark);
        }
        return bookmarks;
    }

    @Override
    protected void verifyModel(Bookmark model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertEquals(VALUES[index], model.getValue());
        verifyLinks(model);
    }

    static Bookmark getModel(int index) {
        Bookmark model = new Bookmark();
        model.setId(GUIDS[index].toString());
        model.setName(NAMES[index]);
        model.setValue(VALUES[index]);
        return model;
    }
}
