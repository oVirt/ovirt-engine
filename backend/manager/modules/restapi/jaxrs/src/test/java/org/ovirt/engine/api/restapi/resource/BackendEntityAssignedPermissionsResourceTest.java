package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.action.PermissionsOperationsParametes;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendEntityAssignedPermissionsResourceTest
        extends AbstractBackendAssignedPermissionsResourceTest {

    public BackendEntityAssignedPermissionsResourceTest() {
        super(GUIDS[2],
              VdcQueryType.GetPermissionsForObject,
              new GetPermissionsForObjectParameters(GUIDS[1]),
              DataCenter.class,
              "VdcUser.UserId",
              "ObjectId");
    }

    @Test
    public void testAddIncompletePermission() throws Exception {
        Permission model = new Permission();
        model.setDataCenter(new DataCenter());
        model.getDataCenter().setId(GUIDS[2].toString());
        model.setRole(new Role());
        model.getRole().setId(GUIDS[3].toString());

        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Permission", "add", "user|group.id");
        }
    }

    @Test
    public void testAddGroupPermission() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddPermission,
                                  PermissionsOperationsParametes.class,
                                  new String[] { "AdGroup.id",
                                                 "Permission.ad_element_id",
                                                 "Permission.ObjectId",
                                                 "Permission.role_id" },
                                  new Object[] { GUIDS[1], GUIDS[1], GUIDS[2], GUIDS[3] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetPermissionById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));
        Permission model = new Permission();
        model.setRole(new Role());
        model.getRole().setId(GUIDS[3].toString());
        model.setGroup(new Group());
        model.getGroup().setId(GUIDS[1].toString());
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Permission);
        verifyModel((Permission) response.getEntity(), 0);
    }

    @Test
    @Override
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpGetEntityExpectations(VdcQueryType.GetDbUserByUserId,
                                    IdQueryParameters.class,
                                    new String[] {"Id"},
                                    new Object[] {GUIDS[1]},
                                    getUserByIdx(1));
        setUpQueryExpectations("");

        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    protected Permission getModel() {
        Permission model = new Permission();
        model.setUser(new User());
        model.getUser().setId(GUIDS[1].toString());
        model.setRole(new Role());
        model.getRole().setId(GUIDS[3].toString());
        return model;
    }

    @Override
    protected void verifyModel(Permission model, int index) {
        super.verifyModel(model, index);
        if (index == 0) {
            assertTrue(model.isSetUser());
            assertEquals(GUIDS[1].toString(), model.getUser().getId());
        } else {
            assertTrue(model.isSetGroup());
            assertEquals(GUIDS[1].toString(), model.getGroup().getId());
        }
    }
}

