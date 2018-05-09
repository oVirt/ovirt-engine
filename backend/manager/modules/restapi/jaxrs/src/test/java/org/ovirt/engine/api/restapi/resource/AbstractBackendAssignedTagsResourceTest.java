package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.TagsActionParametersBase;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public abstract class AbstractBackendAssignedTagsResourceTest<C extends AbstractBackendAssignedTagsResource>
    extends AbstractBackendCollectionResourceTest<Tag, Tags, C> {

    protected static final Guid PARENT_GUID = GUIDS[2];

    protected static String parentIdName;
    protected static QueryType queryType;
    protected static Class<? extends QueryParametersBase> queryParams;
    protected static ActionType attachAction;
    protected static Class<? extends TagsActionParametersBase> attachParams;

    public AbstractBackendAssignedTagsResourceTest(C collection) {
        super(collection, null, "");
    }

    @Test
    @Disabled
    @Override
    public void testQuery() {
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> collection.getTagResource("foo")));
    }

    @Test
    public void testAddTag() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(attachAction,
                                  attachParams,
                                  new String[] { "TagId", "EntitiesId" },
                                  new Object[] { GUIDS[0], asList(PARENT_GUID) },
                                  true,
                                  true,
                                  null,
                                  QueryType.GetTagByTagId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  setUpTags().get(0));

        Tag model = new Tag();
        model.setId(GUIDS[0].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Tag);
        verifyModel((Tag)response.getEntity(), 0);
    }

    @Test
    public void testAddTagByName() {
        setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(QueryType.GetAllTags,
                                     QueryParametersBase.class,
                                     new String[] { },
                                     new Object[] { },
                                     setUpTags());

        setUpCreationExpectations(attachAction,
                                  attachParams,
                                  new String[] { "TagId", "EntitiesId" },
                                  new Object[] { GUIDS[0], asList(PARENT_GUID) },
                                  true,
                                  true,
                                  null,
                                  QueryType.GetTagByTagId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  setUpTags().get(0));

        Tag model = new Tag();
        model.setName(NAMES[0]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Tag);
        verifyModel((Tag)response.getEntity(), 0);
    }

    @Test
    public void testAddIncompleteParameters() {
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(new Tag())), "Tag", "add", "id|name");
    }

    @Test
    public void testAddTagCantDo() {
        doTestBadAddTag(false, true, CANT_DO);
    }

    @Test
    public void testAddTagFailure() {
        doTestBadAddTag(true, false, FAILURE);
    }

    private void doTestBadAddTag(boolean valid, boolean success, String detail) {
        setUriInfo(setUpActionExpectations(attachAction,
                                           attachParams,
                                           new String[] { "TagId", "EntitiesId" },
                                           new Object[] { GUIDS[0], asList(PARENT_GUID) },
                                           valid,
                                           success));
        Tag model = new Tag();
        model.setId(GUIDS[0].toString());

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        assertEquals("", query);

        setUpEntityQueryExpectations(queryType,
                                     queryParams,
                                     new String[] { parentIdName },
                                     new Object[] { PARENT_GUID.toString() },
                                     setUpTags(),
                                     failure);

    }

    @Override
    protected Tags getEntity(int index) {
        return new Tags(DESCRIPTIONS[index], null, false, GUIDS[index], NAMES[index]);
    }

    static List<Tags> setUpTags() {
        List<Tags> tags = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            tags.add(new Tags(DESCRIPTIONS[i], null, false, GUIDS[i], NAMES[i]));
        }
        return tags;
    }

    @Override
    protected List<Tag> getCollection() {
        return collection.list().getTags();
    }

    @Override
    protected void verifyModel(Tag model, int index) {
        super.verifyModel(model, index);
        assertFalse(model.getHref().startsWith(BASE_PATH + "/tags"));
    }
}
