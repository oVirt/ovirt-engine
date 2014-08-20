package org.ovirt.engine.api.restapi.resource.aaa;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Domain;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.aaa.DirectoryUser;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.action.IdParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

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
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveUser,
                IdParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                true,
                true
            )
        );
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(
             VdcQueryType.GetDbUserByUserId,
             IdQueryParameters.class,
             new String[] { "Id" },
             new Object[] { NON_EXISTANT_GUID },
             null
        );
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(
            VdcQueryType.GetDbUserByUserId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getEntity(0)
        );
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    private void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveUser,
                IdParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                canDo,
                success
            )
        );
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddUser_2() throws Exception {
        setUpAddUserExpectations("ADUSER@" + DOMAIN + ":: username=" + NAMES[0]);
        User model = new User();
        Domain domain = new Domain();
        domain.setName(DOMAIN);
        model.setDomain(domain);
        model.setUserName(NAMES[0]);
        Response response = collection.add(model);
        verifyAddUser(response);
    }

    @Test
    public void testAddUser_3() throws Exception {
        setUpAddUserExpectations("ADUSER@" + DOMAIN + ":: username=" + NAMES[0]);
        User model = new User();
        model.setUserName(NAMES[0] + "@" + DOMAIN);

        Response response = collection.add(model);
        verifyAddUser(response);
    }

    @Test
    public void testAddUser_4() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetDomainList,
                VdcQueryParametersBase.class,
                new String[] { },
                new Object[] { },
                setUpDomains());
        setUpAddUserExpectations("ADUSER@" + DOMAIN + ":: username=" + NAMES[0]);
        User model = new User();
        model.setUserName(NAMES[0]);
        Domain domain = new Domain();
        domain.setId(new Guid(DOMAIN.getBytes(Charset.forName("UTF-8")), true).toString());
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

    private void setUpAddUserExpectations(String query) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(
            query,
            SearchType.DirectoryUser,
            getDirectoryUser(0)
        );
        setUpCreationExpectations(
            VdcActionType.AddUser,
            AddUserParameters.class,
            new String[] { "UserToAdd" },
            new Object[] { new DbUser(getDirectoryUser(0)) },
            true,
            true,
            GUIDS[0],
            VdcQueryType.GetDbUserByUserId,
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
        DbUser entity = new DbUser();
        entity.setId(GUIDS[index]);
        entity.setExternalId(EXTERNAL_IDS[index]);
        entity.setLoginName(NAMES[index]);
        entity.setGroupNames(new LinkedList<String>(Arrays.asList(GROUPS.split(","))));
        entity.setNamespace(NAMESPACE);
        entity.setDomain(DOMAIN);
        return entity;
    }

    private DirectoryUser getDirectoryUser(int index) {
        return new DirectoryUser(DOMAIN, NAMESPACE, EXTERNAL_IDS[index], NAMES[index]);
    }

    @Override
    protected void verifyModel(User model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index] + "@" + DOMAIN, model.getUserName());
        assertNotNull(model.getDomain());
        assertEquals(new Guid(DOMAIN.getBytes(Charset.forName("UTF-8")), true).toString(), model.getDomain().getId());
        assertTrue(model.isSetGroups());
        assertEquals(PARSED_GROUPS.length, model.getGroups().getGroups().size());
        HashSet<String> groupNames = new HashSet<>();
        for (Group group : model.getGroups().getGroups()) {
            groupNames.add(group.getName());
        }
        assertEquals(new HashSet<String>(Arrays.asList(PARSED_GROUPS)), groupNames);
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
        try {
            getCollection();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            assertTrue(wae.getResponse().getEntity() instanceof Fault);
            assertEquals(mockl10n(FAILURE), ((Fault) wae.getResponse().getEntity()).getDetail());
        }
    }

    @Override
    @Test
    public void testListCrash() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        Throwable t = new RuntimeException(FAILURE);
        setUpQueryExpectations(QUERY, t);
        collection.setUriInfo(uriInfo);
        try {
            getCollection();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, t);
        }
    }

    @Override
    @Test
    public void testListCrashClientLocale() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        locales.add(CLIENT_LOCALE);
        Throwable t = new RuntimeException(FAILURE);
        setUpQueryExpectations(QUERY, t);
        collection.setUriInfo(uriInfo);
        try {
            getCollection();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_CLIENT_LOCALE, t);
        }
        finally {
            locales.clear();
        }
    }
}
