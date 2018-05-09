package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.PARENT_GUID;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.PARENT_IDX;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.setUpTags;
import static org.ovirt.engine.api.restapi.resource.BackendTagsResourceTest.verifyParent;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MoveTagParameters;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
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
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> new BackendTagResource("foo", null)));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(0, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
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
        setUpGetEntityExpectations(0, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0, false))));
    }

    @Test
    public void testUpdate() {
        setUpGetEntityExpectations(0);
        setUpGetEntityExpectations(0);
        setUpGetEntityExpectations(0);

        setUriInfo(setUpActionExpectations(ActionType.UpdateTag,
                                           TagsOperationParameters.class,
                                           new String[] { "Tag.TagName", "Tag.ParentId" },
                                           new Object[] { NAMES[0], PARENT_GUID },
                                           true,
                                           true));

        verifyModel(resource.update(getModel(0, false)), 0);
    }

    @Test
    public void testMove() {
        doTestMove(getModel(0), 0);
    }

    @Test
    public void testMoveNamedParent() {
        setUpEntityQueryExpectations(QueryType.GetTagByTagName,
                                     NameQueryParameters.class,
                                     new String[] { "Name" },
                                     new Object[] { NAMES[PARENT_IDX] },
                getEntity(NEW_PARENT_IDX));

        Tag model = getModel(0);
        model.getParent().setId(null);
        model.getParent().setName(NAMES[PARENT_IDX]);

        doTestMove(model, 0);
    }

    protected void doTestMove(Tag model, int index) {
        model.getParent().setId(NEW_PARENT_ID.toString());
        setUpActionExpectations(ActionType.MoveTag,
                                MoveTagParameters.class,
                                new String[] { "TagId", "NewParentId" },
                new Object[] { GUIDS[index], NEW_PARENT_ID },
                                true,
                                true,
                                null,
                                null,
                                false);

        setUpGetEntityExpectations(index);

        setUriInfo(setUpActionExpectations(ActionType.UpdateTag,
                                           TagsOperationParameters.class,
                                           new String[] { "Tag.TagName", "Tag.ParentId" },
                new Object[] { NAMES[index], NEW_PARENT_ID },
                                           true,
                                           true));

        setUpGetEntityExcpectations();

        verifyModel(resource.update(model), index, NEW_PARENT_ID.toString());
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
        setUpGetEntityExpectations(0);

        setUriInfo(setUpActionExpectations(ActionType.UpdateTag,
                                           TagsOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           valid,
                                           success));

        verifyFault(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0, false))), detail);
    }

    @Test
    public void testConflictedUpdate() {
        setUpGetEntityExpectations(0);
        setUpGetEntityExpectations(0);
        setUriInfo(setUpBasicUriExpectations());

        Tag model = getModel(1, false);
        model.setId(NEW_PARENT_ID.toString());
        verifyImmutabilityConstraint(assertThrows(WebApplicationException.class, () -> resource.update(model)));
    }

    @Test
    public void testRemove() {
        setUpGetEntityExcpectations();
        setUriInfo(setUpActionExpectations(ActionType.RemoveTag,
                TagsActionParametersBase.class,
                new String[] { "TagId" },
                new Object[] { GUIDS[0] },
                true,
                true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUpGetEntityExpectations(QueryType.GetTagByTagId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                null);
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

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpGetEntityExcpectations();
        setUriInfo(setUpActionExpectations(ActionType.RemoveTag,
                TagsActionParametersBase.class,
                new String[] { "TagId" },
                new Object[] { GUIDS[0] },
                valid,
                success));

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    private void setUpGetEntityExcpectations() {
        setUpGetEntityExpectations(QueryType.GetTagByTagId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));
    }

    protected void setUpGetEntityExpectations(int index) {
        setUpGetEntityExpectations(index, false);
    }

    protected void setUpGetEntityExpectations(int index, boolean notFound) {
        setUpGetEntityExpectations(QueryType.GetTagByTagId,
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
        verifyModel(model, index, PARENT_GUID.toString());
    }

    protected void verifyModel(Tag model, int index, String parentId) {
        super.verifyModel(model, index);
        verifyParent(model, parentId);
    }
}
