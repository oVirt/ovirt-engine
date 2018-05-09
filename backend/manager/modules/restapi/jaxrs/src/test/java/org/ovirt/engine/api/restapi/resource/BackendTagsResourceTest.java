package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.restapi.resource.BaseBackendResource.WebFaultException;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.TagsOperationParameters;
import org.ovirt.engine.core.common.businessentities.Tags;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendTagsResourceTest
    extends AbstractBackendCollectionResourceTest<Tag, Tags, BackendTagsResource> {

    static int PARENT_IDX = NAMES.length-1;
    static Guid PARENT_GUID = GUIDS[PARENT_IDX];

    public BackendTagsResourceTest() {
        super(new BackendTagsResource(), null, "");
    }

    @Test
    @Disabled
    @Override
    public void testQuery() {
    }

    @Test
    public void testListLimitResults() throws Exception {
        UriInfo uriInfo = setUpUriExpectationsWithMax(false);
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        List<Tag> results = getCollection();
        assertNotNull(collection);
        assertEquals(3, results.size());
    }

    @Test
    public void testListLimitResultsBadFormat() {
        UriInfo uriInfo = setUpUriExpectationsWithMax(true);
        setUpEntityQueryExpectations(QueryType.GetAllTags,
                                     QueryParametersBase.class,
                                     new String[] { },
                                     new Object[] { },
                                     setUpTags(),
                                     null);
        collection.setUriInfo(uriInfo);
        assertThrows(WebFaultException.class, this::getCollection);
    }

    @Test
    public void testAddTag() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.AddTag,
                                  TagsOperationParameters.class,
                                  new String[] { "Tag.TagName", "Tag.ParentId" },
                                  new Object[] { NAMES[0], PARENT_GUID },
                                  true,
                                  true,
                                  null,
                                  QueryType.GetTagByTagName,
                                  NameQueryParameters.class,
                                  new String[] { "Name" },
                                  new Object[] { NAMES[0] },
                                  getEntity(0));

        Response response = collection.add(getModel(0));
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Tag);
        verifyModel((Tag)response.getEntity(), 0);
    }

    @Test
    public void testAddTagNamedParent() {
        setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(QueryType.GetTagByTagName,
                                     NameQueryParameters.class,
                                     new String[] { "Name" },
                                     new Object[] { NAMES[PARENT_IDX] },
                                     getEntity(PARENT_IDX));

        setUpCreationExpectations(ActionType.AddTag,
                                  TagsOperationParameters.class,
                                  new String[] { "Tag.TagName", "Tag.ParentId" },
                                  new Object[] { NAMES[0], PARENT_GUID },
                                  true,
                                  true,
                                  null,
                                  QueryType.GetTagByTagName,
                                  NameQueryParameters.class,
                                  new String[] { "Name" },
                                  new Object[] { NAMES[0] },
                                  getEntity(0));

        Tag model = getModel(0);
        model.getParent().setId(null);
        model.getParent().setName(NAMES[PARENT_IDX]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Tag);
        verifyModel((Tag)response.getEntity(), 0);
    }

    @Test
    public void testAddTagNoParent() {
        setUriInfo(setUpBasicUriExpectations());

        Tags entity = getEntity(0);
        entity.setParentId(Guid.Empty);

        setUpCreationExpectations(ActionType.AddTag,
                                  TagsOperationParameters.class,
                                  new String[] { "Tag.TagName", "Tag.ParentId" },
                                  new Object[] { NAMES[0], Guid.Empty },
                                  true,
                                  true,
                                  null,
                                  QueryType.GetTagByTagName,
                                  NameQueryParameters.class,
                                  new String[] { "Name" },
                                  new Object[] { NAMES[0] },
                                  entity);

        Tag model = getModel(0);
        model.setParent(null);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Tag);
        verifyModel((Tag)response.getEntity(), 0, Guid.Empty.toString());
    }

    @Test
    public void testAddIncompleteParameters() {
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(new Tag())), "Tag", "add", "name");
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
        setUriInfo(setUpActionExpectations(ActionType.AddTag,
                                           TagsOperationParameters.class,
                                           new String[] { "Tag.TagName", "Tag.ParentId" },
                                           new Object[] { NAMES[0], PARENT_GUID },
                                           valid,
                                           success));

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(getModel(0))), detail);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        assertEquals("", query);

        setUpEntityQueryExpectations(QueryType.GetAllTags,
                                     QueryParametersBase.class,
                                     new String[] { },
                                     new Object[] { },
                                     setUpTags(),
                                     failure);

        if (failure == null) {
            setUpEntityQueryExpectations(QueryType.GetRootTag,
                                         QueryParametersBase.class,
                                         new String[] { },
                                         new Object[] { },
                                         setUpRootTag());
        }

    }

    @Override
    protected Tags getEntity(int index) {
        return new Tags(DESCRIPTIONS[index], PARENT_GUID, false, GUIDS[index], NAMES[index]);
    }

    static Tags setUpRootTag() {
        return new Tags("root", null, true, Guid.Empty, "root");
    }

    static List<Tags> setUpTags() {
        List<Tags> tags = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            tags.add(new Tags(DESCRIPTIONS[i], PARENT_GUID, false, GUIDS[i], NAMES[i]));
        }
        return tags;
    }

    @Override
    protected List<Tag> getCollection() {
        return collection.list().getTags();
    }

    static Tag getModel(int index) {
        return getModel(index, true);
    }

    static Tag getModel(int index, boolean includeParent) {
        Tag model = new Tag();
        model.setId(GUIDS[index].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        if (includeParent) {
            model.setParent(new Tag());
            model.getParent().setId(PARENT_GUID.toString());
        }
        return model;
    }

    @Override
    protected void verifyCollection(List<Tag> collection) throws Exception {
        assertNotNull(collection);
        assertEquals(NAMES.length + 1, collection.size());
        verifyRoot(collection.get(NAMES.length));
        collection.remove(NAMES.length);
        super.verifyCollection(collection);
    }

    @Override
    protected void verifyModel(Tag model, int index) {
        verifyModel(model, index, PARENT_GUID.toString());
    }

    protected void verifyModel(Tag model, int index, String parentId) {
        super.verifyModel(model, index);
        verifyParent(model, parentId);
    }

    static void verifyParent(Tag model, String parentId) {
        assertNotNull(model.getParent());
        assertEquals(parentId, model.getParent().getId());
    }

    protected void verifyRoot(Tag root) {
        assertEquals(Guid.Empty.toString(), root.getId());
        assertEquals("root", root.getName());
        assertEquals("root", root.getDescription());
        assertNull(root.getParent());
        verifyLinks(root);
    }

    @SuppressWarnings("unchecked")
    protected UriInfo setUpUriExpectationsWithMax(boolean badFormat) {
        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getBaseUri()).thenReturn(URI.create(URI_BASE));
        List<PathSegment> psl = new ArrayList<>();

        PathSegment ps = mock(PathSegment.class);
        MultivaluedMap<String, String> matrixParams = mock(MultivaluedMap.class);

        when(matrixParams.isEmpty()).thenReturn(false);
        when(matrixParams.containsKey("max")).thenReturn(true);
        when(matrixParams.getFirst("max")).thenReturn(badFormat ? "bla3" : "2");
        when(ps.getMatrixParameters()).thenReturn(matrixParams);

        psl.add(ps);

        when(uriInfo.getPathSegments()).thenReturn(psl);
        return uriInfo;
    }
}
