package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.api.model.Permits;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.core.common.action.RoleWithActionGroupsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.RoleType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationsQueriesParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendRolesResourceTest
        extends AbstractBackendCollectionResourceTest<Role, org.ovirt.engine.core.common.businessentities.Role, BackendRolesResource> {

    public BackendRolesResourceTest() {
        super(new BackendRolesResource(), null, "");
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Test
    public void testAddRole() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddRoleWithActionGroups,
                                  RoleWithActionGroupsParameters.class,
                                  new String[] { "Role.Id", "Role.Name" },
                                  new Object[] { GUIDS[0], NAMES[0] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetRoleById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        Role model = new Role();
        model.setName(NAMES[0]);
        model.setPermits(new Permits());
        model.getPermits().getPermits().add(new Permit());
        model.getPermits().getPermits().get(0).setId(""+ActionGroup.CREATE_VM.getId());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Role);
        verifyModel((Role) response.getEntity(), 0);
    }

    @Test
    public void testAddRoleInvalidPermit() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        Role model = new Role();
        model.setName(NAMES[0]);
        model.setPermits(new Permits());
        model.getPermits().getPermits().add(new Permit());
        model.getPermits().getPermits().get(0).setId("1234");

        try {
            Response response = collection.add(model);
            fail("expected WebApplicationException");
        } catch(WebApplicationException wae) {
            assertEquals(BAD_REQUEST, wae.getResponse().getStatus());
            assertEquals("1234 is not a valid permit ID.", wae.getResponse().getEntity());
        }
    }

    @Test
    public void testAddIncompleteParametersNoPermits() throws Exception {
        Role model = new Role();
        model.setName(NAMES[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Role", "add", "permits.id");
        }
    }

    @Test
    public void testAddIncompleteParametersNoName() throws Exception {
        Role model = new Role();
        model.setPermits(new Permits());
        model.getPermits().getPermits().add(new Permit());
        model.getPermits().getPermits().get(0).setId("1");
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Role", "add", "name");
        }
    }

    @Override
    protected List<Role> getCollection() {
        return collection.list().getRoles();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetAllRoles,
                                     MultilevelAdministrationsQueriesParameters.class,
                                     new String[] { },
                                     new Object[] { },
                                     setUpRoles(),
                                     failure);

        control.replay();
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Role getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.Role role = new org.ovirt.engine.core.common.businessentities.Role();
        role.setId(GUIDS[index]);
        role.setName(NAMES[index]);
        role.setDescription(DESCRIPTIONS[index]);
        role.setReadonly(false);
        role.setType(RoleType.ADMIN);
        return role;
    }

    protected List<org.ovirt.engine.core.common.businessentities.Role> setUpRoles() {
        List<org.ovirt.engine.core.common.businessentities.Role> roles = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            roles.add(getEntity(i));
        }
        return roles;
    }

    @Override
    protected void verifyModel(Role model, int index) {
        super.verifyModel(model, index);
        assertTrue(model.isMutable());
        assertTrue(model.isAdministrative());
    }
}
