package org.ovirt.engine.api.restapi.resource;

import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.PARENT_GUID;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.PARENT_IDX;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.setUpTags;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.verifyParent;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.core.common.action.MoveTagParameters;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendTagResourceTest
    extends AbstractBackendSubResourceTest<Tag, Tags, BackendTagResource> {

    private static final int NEW_PARENT_IDX = 1;
    private static final Guid NEW_PARENT_ID = GUIDS[NEW_PARENT_IDX];

    public BackendTagResourceTest() {
        super(new BackendTagResource(GUIDS[0].toString(), new BackendTagsResource()));
    }

    @Override
    protected void init() {
        super.init();
        initResource(resource.getParent());
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendTagResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
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
        setUpGetEntityExpectations(0, true);
        control.replay();
        try {
            resource.update(getModel(0, false));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(0);
        setUpGetEntityExpectations(0);
        setUpGetEntityExpectations(0);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateTag,
                                           TagsOperationParameters.class,
                                           new String[] { "Tag.TagName", "Tag.ParentId" },
                                           new Object[] { NAMES[0], PARENT_GUID },
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0, false)), 0);
    }

    @Test
    public void testMove() throws Exception {
        doTestMove(getModel(0), 0);
    }

    @Test
    public void testMoveNamedParent() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetTagByTagName,
                                     NameQueryParameters.class,
                                     new String[] { "Name" },
                                     new Object[] { NAMES[PARENT_IDX] },
                getEntity(NEW_PARENT_IDX));

        Tag model = getModel(0);
        model.getParent().setId(null);
        model.getParent().setName(NAMES[PARENT_IDX]);

        doTestMove(model, 0);
    }

    protected void doTestMove(Tag model, int index) throws Exception {
        model.getParent().setId(NEW_PARENT_ID.toString());
        setUpActionExpectations(VdcActionType.MoveTag,
                                MoveTagParameters.class,
                                new String[] { "TagId", "NewParentId" },
                new Object[] { GUIDS[index], NEW_PARENT_ID },
                                true,
                                true,
                                null,
                                null,
                                false);

        setUpGetEntityExpectations(index);
        setUpGetEntityExpectations(index);
        setUpGetEntityExpectations(index);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateTag,
                                           TagsOperationParameters.class,
                                           new String[] { "Tag.TagName", "Tag.ParentId" },
                new Object[] { NAMES[index], NEW_PARENT_ID },
                                           true,
                                           true));

        verifyModel(resource.update(model), index);
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
        setUpGetEntityExpectations(0);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateTag,
                                           TagsOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           valid,
                                           success));

        try {
            resource.update(getModel(0, false));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUpGetEntityExpectations(0);
        setUpGetEntityExpectations(0);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();

        Tag model = getModel(1, false);
        model.setId(NEW_PARENT_ID.toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExcpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveTag,
                TagsActionParametersBase.class,
                new String[] { "TagId" },
                new Object[] { GUIDS[0] },
                true,
                true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetTagByTagId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                null);
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
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

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUpGetEntityExcpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveTag,
                TagsActionParametersBase.class,
                new String[] { "TagId" },
                new Object[] { GUIDS[0] },
                valid,
                success));
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    private void setUpGetEntityExcpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetTagByTagId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));
    }

    protected void setUpGetEntityExpectations(int index) throws Exception {
        setUpGetEntityExpectations(index, false);
    }

    protected void setUpGetEntityExpectations(int index, boolean notFound) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetTagByTagId,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[index] },
                                   notFound ? null : getEntity(index));
    }

    @Override
    protected Tags getEntity(int index) {
        return setUpTags().get(index);
    }

    @Override
    protected void verifyModel(Tag model, int index) {
        super.verifyModel(model, index);
        verifyParent(model, PARENT_GUID.toString());
    }
}
