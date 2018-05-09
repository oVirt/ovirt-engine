package org.ovirt.engine.api.restapi.resource.aaa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.api.restapi.utils.DirectoryEntryIdUtils;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendUsersResourceTest
    extends AbstractBackendCollectionResourceTest<User, DbUser, BackendUsersResource> {

    static final String NAMESPACE = "*";

    static final String GROUPS =
        "Schema Admins@Maghreb/Users," +
        "Group Policy Creator Owners@Maghreb/Users," +
        "Enterprise Admins@Maghreb/Users";

    static final String[] PARSED_GROUPS = {
        "Schema Admins@Maghreb/Users",
        "Group Policy Creator Owners@Maghreb/Users",
        "Enterprise Admins@Maghreb/Users",
    };

    protected static final String SEARCH_QUERY = "name=s* AND id=*0 and usrname != \"\"";
    protected static final String QUERY = "usrname != \"\"";

    public BackendUsersResourceTest() {
        super(new BackendUsersResource(), SearchType.DBUser, "Users : ");
    }

    @Test
    public void testAddUser2() {
        setUpEntityQueryExpectations(QueryType.GetDomainList,
                QueryParametersBase.class,
                new String[] {},
                new Object[] {},
                setUpDomains());
        setUpAddUserExpectations("ADUSER@" + DOMAIN + ":: username=" + NAMES[0]);
        User model = new User();
        Domain domain = new Domain();
        domain.setName(DOMAIN);
        domain.setId(DirectoryEntryIdUtils.encode(domain.getName()));
        model.setDomain(domain);
        model.setUserName(NAMES[0]);
        Response response = collection.add(model);
        verifyAddUser(response);
    }

    @Test
    public void testAddUser3() {
        setUpEntityQueryExpectations(QueryType.GetDomainList,
                QueryParametersBase.class,
                new String[] {},
                new Object[] {},
                setUpDomains());
        setUpAddUserExpectations("ADUSER@" + DOMAIN + ":: username=" + NAMES[0]);
        User model = new User();
        model.setUserName(NAMES[0] + "@" + DOMAIN);

        Response response = collection.add(model);
        verifyAddUser(response);
    }

    @Test
    public void testAddUser4() {
        setUpEntityQueryExpectations(QueryType.GetDomainList,
                QueryParametersBase.class,
                new String[] { },
                new Object[] { },
                setUpDomains());
        setUpAddUserExpectations("ADUSER@" + DOMAIN + ":: username=" + NAMES[0]);
        User model = new User();
        model.setUserName(NAMES[0]);
        Domain domain = new Domain();
        domain.setName(DOMAIN);
        domain.setId(DirectoryEntryIdUtils.encode(domain.getName()));
        model.setDomain(domain);
        Response response = collection.add(model);
        verifyAddUser(response);
    }

    private List<String> setUpDomains() {
        List<String> domains = new LinkedList<>();
        domains.add("some.domain");
        domains.add(DOMAIN);
        return domains;
    }

    private void verifyAddUser(Response response) {
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof User);
        verifyModel((User) response.getEntity(), 0);
    }

    private void setUpAddUserExpectations(String query) {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(
            query,
            SearchType.DirectoryUser,
            getDirectoryUser(0)
        );
        setUpCreationExpectations(
            ActionType.AddUser,
            AddUserParameters.class,
            new String[] { "UserToAdd" },
            new Object[] { new DbUser(getDirectoryUser(0)) },
            true,
            true,
            GUIDS[0],
            QueryType.GetDbUserByUserId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getEntity(0)
        );
    }

    @Override
    protected List<User> getCollection() {
        return collection.list().getUsers();
    }

    @Override
    protected DbUser getEntity(int index) {
        DbUser entity = new DbUser(getDirectoryUser(index));
        entity.setGroupNames(new LinkedList<>(Arrays.asList(GROUPS.split(","))));
        entity.setId(GUIDS[index]);
        return entity;
    }

    private DirectoryUser getDirectoryUser(int index) {
        return new DirectoryUser(DOMAIN, NAMESPACE, EXTERNAL_IDS[index], NAMES[index], NAMES[index], "");
    }

    @Override
    protected void verifyModel(User model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index] + "@" + DOMAIN, model.getUserName());
        assertNotNull(model.getDomain());
        assertEquals(DirectoryEntryIdUtils.encode(DOMAIN), model.getDomain().getId());
        assertTrue(model.isSetGroups());
        assertEquals(PARSED_GROUPS.length, model.getGroups().getGroups().size());
        Set<String> groupNames = model.getGroups().getGroups().stream().map(Group::getName).collect(Collectors.toSet());
        assertEquals(new HashSet<>(Arrays.asList(PARSED_GROUPS)), groupNames);
        verifyLinks(model);
    }

    @Override
    protected void setUpQueryExpectations(String query) throws Exception {
        setUpQueryExpectations(query, null);
    }

    @Override
    @Test
    public void testQuery() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(SEARCH_QUERY);
        setUpQueryExpectations(SEARCH_QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpQueryExpectations(QUERY);
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testListFailure() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpQueryExpectations(QUERY, FAILURE);
        collection.setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, this::getCollection));
    }

    @Override
    @Test
    public void testListCrash() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        Throwable t = new RuntimeException(FAILURE);
        setUpQueryExpectations(QUERY, t);
        collection.setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_SERVER_LOCALE, t);
    }

    @Override
    @Test
    public void testListCrashClientLocale() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        locales.add(CLIENT_LOCALE);
        Throwable t = new RuntimeException(FAILURE);
        setUpQueryExpectations(QUERY, t);
        collection.setUriInfo(uriInfo);
        verifyFault(assertThrows(WebApplicationException.class, this::getCollection), BACKEND_FAILED_CLIENT_LOCALE, t);
    }
}
