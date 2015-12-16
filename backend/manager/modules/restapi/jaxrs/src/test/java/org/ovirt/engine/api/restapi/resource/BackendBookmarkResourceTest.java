package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendBookmarksResourceTest.VALUES;
import static org.ovirt.engine.api.restapi.resource.BackendBookmarksResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendBookmarksResourceTest.setUpBookmarks;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Bookmark;
import org.ovirt.engine.core.common.action.BookmarksOperationParameters;
import org.ovirt.engine.core.common.action.BookmarksParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendBookmarkResourceTest extends AbstractBackendSubResourceTest<Bookmark,
    org.ovirt.engine.core.common.businessentities.Bookmark, BackendBookmarkResource> {

    public BackendBookmarkResourceTest() {
        super(new BackendBookmarkResource(GUIDS[0].toString()));
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendBookmarkResource("foo");
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        //Get will return 404
        setUpGetEntityExpectations(0, true);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUpGetEntityExpectations(0);
        setUriInfo(setUpBasicUriExpectations());

        control.replay();

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        //Get will return 404
        setUpGetEntityExpectations(0, true);
        control.replay();
        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(0);
        setUpGetEntityExpectations(0);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateBookmark, BookmarksOperationParameters.class,
                new String[] { "Bookmark.Id", "Bookmark.Name", "Bookmark.Value" },
                new Object[] { GUIDS[0], NAMES[0], VALUES[0] }, true, true));

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(0);


        setUriInfo(setUpActionExpectations(VdcActionType.UpdateBookmark, BookmarksOperationParameters.class,
                new String[] {}, new Object[] {}, valid, success));

        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(0);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveBookmark,
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
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(0, true);
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    private void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(0);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveBookmark,
                BookmarksParametersBase.class,
                new String[] { "BookmarkId" },
                new Object[] { GUIDS[0] },
                valid,
                success
            )
        );
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
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

    protected void setUpGetEntityExpectations(int index) throws Exception {
        setUpGetEntityExpectations(index, false);
    }

    protected void setUpGetEntityExpectations(int index, boolean notFound) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetBookmarkByBookmarkId,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[index] },
                                   notFound ? null : getEntity(index));
    }
}
