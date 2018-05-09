package org.ovirt.engine.api.restapi.resource.aaa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.api.restapi.utils.DirectoryEntryIdUtils;
import org.ovirt.engine.core.aaa.DirectoryGroup;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddGroupParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.DirectoryIdQueryParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendGroupsResourceTest
    extends AbstractBackendCollectionResourceTest<Group, DbGroup, BackendGroupsResource> {

    private static final String NAMESPACE = "*";

    /**
     * This is the query that will be used when the user didn't provide any query explicitly.
     */
    private static final String QUERY = "grpname != \"\"";

    /**
     * This is the query that will be used when the user provided a query explicitly in the parameters.
     */
    private static final String SEARCH_QUERY =
        "name=s* AND id=*0 and grpname != \"\"";

    /**
     * These are the names that will be used to build the directory group objects returned by mocked directory group
     * searches, thus then must have the same format that we generate when searching in directories.
     */
    private static final String[] GROUP_NAMES;
    private static final String[] GROUP_NAMES_WITH_NO_DOMAIN;

    static {
        GROUP_NAMES = new String[NAMES.length];
        GROUP_NAMES_WITH_NO_DOMAIN = new String[NAMES.length];
        for (int i = 0; i < NAMES.length; i++) {
            GROUP_NAMES_WITH_NO_DOMAIN[i] = "Groups/" + NAMES[i];
            GROUP_NAMES[i] = GROUP_NAMES_WITH_NO_DOMAIN[i] + "@" + DOMAIN;
        }
    }

    public BackendGroupsResourceTest() {
        super(new BackendGroupsResource(), SearchType.DBGroup, "Groups : ");
    }

    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpQueryExpectations(QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testListFailure() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpQueryExpectations(QUERY, FAILURE);
        collection.setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, this::getCollection));
    }

    @Test
    public void testListCrash() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        Throwable t = new RuntimeException(FAILURE);
        setUpQueryExpectations(QUERY, t);
        collection.setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_SERVER_LOCALE, t);
    }

    @Test
    public void testListCrashClientLocale() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        locales.add(CLIENT_LOCALE);
        Throwable t = new RuntimeException(FAILURE);
        setUpQueryExpectations(QUERY, t);
        collection.setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_CLIENT_LOCALE, t);
    }

    @Test
    public void testQuery() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(SEARCH_QUERY);
        setUpQueryExpectations(SEARCH_QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    /**
     * Test that a group can be added when the user provides explicitly the name of the directory, so there is no need
     * to extract it from the name of the group.
     */
    @Test
    public void testAddGroupWithExplicitDirectoryName() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetDomainList,
                QueryParametersBase.class,
                new String[] {},
                new Object[] {},
                setUpDomains());
        setUpGetEntityExpectations(
                "ADGROUP@" + DOMAIN + ":: name=" + GROUP_NAMES_WITH_NO_DOMAIN[0],
            SearchType.DirectoryGroup,
            getDirectoryGroup(0)
        );
        DbGroup dbGroup = new DbGroup(getDirectoryGroup(0));
        setUpCreationExpectations(
            ActionType.AddGroup,
            AddGroupParameters.class,
            new String[] { "GroupToAdd" },
            new Object[] { dbGroup },
            true,
            true,
            dbGroup.getId(),
            QueryType.GetDbGroupById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { dbGroup.getId()},
            getEntity(0)
        );

        Domain domain = new Domain();
        domain.setName(DOMAIN);
        domain.setId(DirectoryEntryIdUtils.encode(domain.getName()));
        Group model = new Group();
        model.setName(GROUP_NAMES_WITH_NO_DOMAIN[0]);
        model.setDomain(domain);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Group);
        verifyModel((Group) response.getEntity(), 0);
    }

    private List<String> setUpDomains() {
        List<String> domains = new LinkedList<>();
        domains.add("some.domain");
        domains.add(DOMAIN);
        return domains;
    }

    /**
     * Test that a group can be added when the user doesn't explicitly provide the name of the directory, but provides
     * it as part of the group name.
     */
    @Test
    public void testAddGroupWithImplicitDirectoryName() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetDomainList,
                QueryParametersBase.class,
                new String[] {},
                new Object[] {},
                setUpDomains());
        setUpGetEntityExpectations(
                "ADGROUP@" + DOMAIN + ":: name=" + GROUP_NAMES_WITH_NO_DOMAIN[0],
            SearchType.DirectoryGroup,
            getDirectoryGroup(0)
        );
        setUpCreationExpectations(
            ActionType.AddGroup,
            AddGroupParameters.class,
                new String[] { "GroupToAdd" },
                new Object[] { new DbGroup(getDirectoryGroup(0)) },
            true,
            true,
            GUIDS[0],
            QueryType.GetDbGroupById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getEntity(0)
        );

        Group model = new Group();
        model.setName(GROUP_NAMES[0]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Group);
        verifyModel((Group) response.getEntity(), 0);
    }

    /**
     * Test that a group can't be added if the directory name isn't provider explicitly or as part of the group name.
     */
    @Test
    public void testAddGroupWithoutDirectoryName() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetDomainList,
                QueryParametersBase.class,
                new String[] {},
                new Object[] {},
                setUpDomains());

        Group model = new Group();
        model.setName(GROUP_NAMES_WITH_NO_DOMAIN[0]);

        verifyBadRequest(assertThrows(WebApplicationException.class, () -> collection.add(model)));
    }

    /**
     * Test that if the group identifier is provided it is used to search in the directory instead of the name.
     */
    @Test
    public void testAddGroupById() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetDomainList,
                QueryParametersBase.class,
                new String[] {},
                new Object[] {},
                setUpDomains());
        setUpGetEntityExpectations(
            QueryType.GetDirectoryGroupById,
            DirectoryIdQueryParameters.class,
            new String[] { "Domain", "Id" },
            new Object[] { DOMAIN, DirectoryEntryIdUtils.decode(EXTERNAL_IDS[0]) },
            getDirectoryGroup(0)
        );
        setUpCreationExpectations(
            ActionType.AddGroup,
            AddGroupParameters.class,
            new String[] { "GroupToAdd" },
            new Object[] { new DbGroup(getDirectoryGroup(0)) },
            true,
            true,
            GUIDS[0],
            QueryType.GetDbGroupById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getEntity(0)
        );

        Group model = new Group();
        model.setName(GROUP_NAMES[0]);
        model.setId(EXTERNAL_IDS[0]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Group);
        verifyModel((Group) response.getEntity(), 0);
    }

    /**
     * Test that if the provided directory identifier doesn't correspond to any existing directory user the user isn't
     * added.
     */
    @Test
    public void testAddGroupByIdFailure() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetDomainList,
                QueryParametersBase.class,
                new String[] {},
                new Object[] {},
                setUpDomains());
        setUpGetEntityExpectations(
            QueryType.GetDirectoryGroupById,
            DirectoryIdQueryParameters.class,
            new String[] { "Domain", "Id" },
                new Object[] { DOMAIN, DirectoryEntryIdUtils.decode(NON_EXISTANT_EXTERNAL_ID) },
            null
        );
        Group model = new Group();
        model.setName(GROUP_NAMES[0]);
        model.setId(NON_EXISTANT_EXTERNAL_ID);

        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> collection.add(model)));
    }

    @Override
    protected List<Group> getCollection() {
        return collection.list().getGroups();
    }

    @Override
    protected DbGroup getEntity(int index) {
        return new DbGroup(new DirectoryGroup(DOMAIN, NAMESPACE, EXTERNAL_IDS[index], GROUP_NAMES[index], ""));
    }

    private DirectoryGroup getDirectoryGroup(int index) {
        return new DirectoryGroup(DOMAIN, NAMESPACE, EXTERNAL_IDS[index], GROUP_NAMES[index], "");
    }

    @Override
    protected void verifyModel(Group model, int index) {
        DbGroup entity = getEntity(index);
        assertEquals(entity.getId().toString(), model.getId());
        assertEquals(entity.getName(), model.getName());
        assertNotNull(model.getDomain());
        assertEquals(DirectoryEntryIdUtils.encode(entity.getDomain()), model.getDomain().getId());
        verifyLinks(model);
    }
}
