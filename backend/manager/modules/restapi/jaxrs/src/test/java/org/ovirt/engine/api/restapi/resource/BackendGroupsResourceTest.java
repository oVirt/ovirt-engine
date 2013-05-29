package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.core.common.action.AdElementParametersBase;
import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.LdapGroup;
import org.ovirt.engine.core.common.businessentities.LdapRefStatus;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendGroupsResourceTest
    extends AbstractBackendCollectionResourceTest<Group, LdapGroup, BackendGroupsResource> {

    public BackendGroupsResourceTest() {
        super(new BackendGroupsResource(), null, "");
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Test
    @Override
    @Ignore
    //TODO: revisit when fixed #699242
    public void testList() throws Exception {
    }

    @Test
    @Override
    @Ignore
    //TODO: revisit when fixed #699242
    public void testListFailure() throws Exception {

    }

    @Test
    @Override
    @Ignore
    //TODO: revisit when fixed #699242
    public void testListCrash() throws Exception {

    }

    @Test
    @Override
    @Ignore
    //TODO: revisit when fixed #699242
    public void testListCrashClientLocale() throws Exception {

    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveAdGroup,
                                           AdElementParametersBase.class,
                                           new String[] { "AdElementId" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(NON_EXISTANT_GUID, true);
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
        setUpGetEntityExpectations(GUIDS[0], false);
    }

    private void setUpGetEntityExpectations(Guid entityId, Boolean returnNull) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetAdGroupById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { entityId },
                returnNull ? null : getEntity(0));
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
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveAdGroup,
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
    public void testAddGroup() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations("ADGROUP@" + DOMAIN + ": name=*",
                                   SearchType.AdGroup,
                                   getAdGroup(0));
        setUpCreationExpectations(VdcActionType.AddUser,
                                  AddUserParameters.class,
                                  new String[] { "AdGroup.id" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetAdGroupById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        Group model = new Group();
        model.setName(NAMES[0]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Group);
        verifyModel((Group) response.getEntity(), 0);
    }

    @Test
    public void testAddGroupFailure() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations("ADGROUP@" + DOMAIN + ": name=*",
                                   SearchType.AdGroup,
                                   getAdGroup(0));
        control.replay();
        Group model = new Group();
        model.setName(BAD_NAMES[0]);

        try {
           Response response = collection.add(model);
           fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testAddGroupUsingDomainFromGroupName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations("ADGROUP@" + DOMAIN + ": name=*",
                                   SearchType.AdGroup,
                                   getAdGroupWithDomain(0));
        setUpCreationExpectations(VdcActionType.AddUser,
                                  AddUserParameters.class,
                                  new String[] { "AdGroup.id" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetAdGroupById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        Group model = new Group();
        model.setName(DOMAIN+"/"+NAMES[0]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Group);
        verifyModel((Group) response.getEntity(), 0);
    }

    @Test
    public void testAddGroupById() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(VdcQueryType.GetAdGroupById,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[0] },
                                   getEntity(0));
        setUpCreationExpectations(VdcActionType.AddUser,
                                  AddUserParameters.class,
                                  new String[] { "AdGroup.id" },
                                  new Object[] { GUIDS[0] },
                                  true,
                                  true,
                                  null,
                                  VdcQueryType.GetAdGroupById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        Group model = new Group();
        model.setName(NAMES[0]);
        model.setId(GUIDS[0].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Group);
        verifyModel((Group) response.getEntity(), 0);
    }

    @Test
    public void testAddGroupByIdFailure() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(NON_EXISTANT_GUID, true);
        control.replay();
        Group model = new Group();
        model.setName(NAMES[0]);
        model.setId(NON_EXISTANT_GUID.toString());

        try {
           Response response = collection.add(model);
           fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Override
    protected List<Group> getCollection() {
        return collection.list().getGroups();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assert(query.equals(""));

        setUpEntityQueryExpectations(VdcQueryType.GetAllAdGroups,
                                     VdcQueryParametersBase.class,
                                     new String[] { },
                                     new Object[] { },
                                     setUpGroups(),
                                     failure);

        control.replay();
    }

    @Override
    protected LdapGroup getEntity(int index) {
        LdapGroup entity = new LdapGroup();
        entity.setid(GUIDS[index]);
        entity.setname(NAMES[index]);
        entity.setdomain(DOMAIN);

        return entity;
    }

    protected List<LdapGroup> setUpGroups() {
        List<LdapGroup> groups = new ArrayList<LdapGroup>();
        for (int i = 0; i < NAMES.length; i++) {
            groups.add(getEntity(i));
        }
        return groups;
    }

    protected LdapGroup getAdGroup(int index) {
        LdapGroup adGroup = new LdapGroup();
        adGroup.setid(GUIDS[index]);
        adGroup.setname(NAMES[index]);
        adGroup.setdomain(DOMAIN);

        return adGroup;
    }

    protected LdapGroup getAdGroupWithDomain(int index) {
        LdapGroup adGroup = new LdapGroup();
        adGroup.setid(GUIDS[index]);
        adGroup.setname(DOMAIN+"/"+NAMES[index]);
        adGroup.setdomain(DOMAIN);

        return adGroup;
    }

    @Override
    protected void verifyModel(Group model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertNotNull(model.getDomain());
        assertEquals(new Guid(DOMAIN.getBytes(), true).toString(), model.getDomain().getId());
        verifyLinks(model);
    }

    public static LdapGroup setUpEntityExpectations(LdapGroup entity, int index) {
        expect(entity.getid()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getdomain()).andReturn(DOMAIN).anyTimes();
        expect(entity.getstatus()).andReturn(LdapRefStatus.Active).anyTimes();
        return entity;
    }
}

