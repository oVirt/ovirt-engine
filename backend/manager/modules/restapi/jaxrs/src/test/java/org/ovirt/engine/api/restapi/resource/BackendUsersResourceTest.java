package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqSearchParams;

import java.util.ArrayList;
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
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.LdapUser;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendUsersResourceTest
    extends AbstractBackendCollectionResourceTest<User, DbUser, BackendUsersResource> {

    static final String GROUPS =
        "Schema Admins@Maghreb/Users,Group Policy Creator Owners@Maghreb/Users,Enterprise Admins@Maghreb/Users";
    static final String[] PARSED_GROUPS =
        { "Schema Admins@Maghreb/Users", "Group Policy Creator Owners@Maghreb/Users", "Enterprise Admins@Maghreb/Users" };

    protected static final String SEARCH_QUERY = "Users : usrname != \"\" and name=s* AND id=*0";
    protected static final String QUERY = "Users : usrname != \"\"";

    public BackendUsersResourceTest() {
        super(new BackendUsersResource(), SearchType.DBUser, "Users : ");
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveUser,
                                           AdElementParametersBase.class,
                                           new String[] { "AdElementId" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(VdcQueryType.GetDbUserByUserId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { NON_EXISTANT_GUID },
                null);
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetDbUserByUserId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveUser,
                                           AdElementParametersBase.class,
                                           new String[] { "AdElementId" },
                                           new Object[] { GUIDS[0] },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddUser_2() throws Exception {
        setUpAddUserExpectations("ADUSER@" + DOMAIN + ": allnames=" + NAMES[0]);
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
        setUpAddUserExpectations("ADUSER@" + DOMAIN + ": allnames=" + NAMES[0]+"@"+ DOMAIN);
        User model = new User();
        model.setUserName(NAMES[0]+"@"+DOMAIN);

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
        setUpAddUserExpectations("ADUSER@" + DOMAIN + ": allnames=" + NAMES[0]);
        User model = new User();
        model.setUserName(NAMES[0]);
        Domain domain = new Domain();
        domain.setId(new Guid(DOMAIN.getBytes(), true).toString());
        model.setDomain(domain);
        Response response = collection.add(model);
        verifyAddUser(response);
    }

    private List<String> setUpDomains() {
        List<String> domains = new LinkedList<String>();
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
        setUpGetEntityExpectations(query,
                                   SearchType.AdUser,
                                   getAdUser(0));
        setUpCreationExpectations(VdcActionType.AddUser,
                                  AddUserParameters.class,
                                  new String[] { "VdcUser.UserId" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetDbUserByUserId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
    }

    @Override
    protected List<User> getCollection() {
        return collection.list().getUsers();
    }

    @Override
    protected DbUser getEntity(int index) {
        DbUser entity = new DbUser();
        entity.setId(GUIDS[index]);
        entity.setLoginName(NAMES[index]);
        entity.setGroupNames(GROUPS);
        entity.setDomain(DOMAIN);
        return entity;
    }

    protected LdapUser getAdUser(int index) {
        LdapUser adUser = new LdapUser();
        adUser.setUserId(GUIDS[index]);
        adUser.setUserName(NAMES[index]);
        adUser.setDomainControler(DOMAIN);
        return adUser;
    }

    @Override
    protected void verifyModel(User model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getUserName());
        assertNotNull(model.getDomain());
        assertEquals(new Guid(DOMAIN.getBytes(), true).toString(), model.getDomain().getId());
        assertTrue(model.isSetGroups());
        assertEquals(PARSED_GROUPS.length, model.getGroups().getGroups().size());
        for (int i = 0 ; i < PARSED_GROUPS.length ; i++) {
            Group group = model.getGroups().getGroups().get(i);
            assertEquals(PARSED_GROUPS[i], group.getName());
        }
        verifyLinks(model);
    }

    public static LdapUser setUpEntityExpectations(LdapUser entity, int index) {
        expect(entity.getUserId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getDomainControler()).andReturn(DOMAIN).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        return entity;
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        SearchParameters params = new SearchParameters(query, searchType);
        expect(queryResult.getSucceeded()).andReturn(failure == null).anyTimes();
        if (failure == null) {
            List<DbUser> entities = new ArrayList<DbUser>();
            for (int i = 0; i < NAMES.length; i++) {
                entities.add(getEntity(i));
            }
            expect(queryResult.getReturnValue()).andReturn(entities).anyTimes();
        } else {
            if (failure instanceof String) {
                expect(queryResult.getExceptionString()).andReturn((String) failure).anyTimes();
                setUpL10nExpectations((String)failure);
            } else if (failure instanceof Exception) {
                expect(queryResult.getExceptionString()).andThrow((Exception) failure).anyTimes();
            }
        }
        expect(backend.RunQuery(eq(VdcQueryType.Search), eqSearchParams(params))).andReturn(
                queryResult);
        control.replay();
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
        } catch (WebApplicationException wae) {
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
        } catch (WebApplicationException wae) {
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
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_CLIENT_LOCALE, t);
        } finally {
            locales.clear();
        }
    }
}
