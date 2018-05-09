package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendUserAssignedPermissionsResourceTest
        extends AbstractBackendAssignedPermissionsResourceTest {

    public BackendUserAssignedPermissionsResourceTest() {
        super(GUIDS[1],
              QueryType.GetPermissionsByAdElementId,
              new IdQueryParameters(GUIDS[1]),
              User.class,
              "User.Id",
              "Id");
    }

    @Test
    public void testAddIncompletePermission() {
        Permission model = new Permission();
        model.setUser(new User());
        model.getUser().setId(GUIDS[1].toString());
        model.setRole(new Role());
        model.getRole().setId(GUIDS[3].toString());

        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "Permission",
                "add",
                "dataCenter|cluster|host|storageDomain|vm|vmPool|template.id");
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpGetEntityExpectations(QueryType.GetAnyDbUserByUserId,
                IdQueryParameters.class,
                new String[] {"Id"},
                new Object[] {GUIDS[1]},
                getUserByIdx(1),
                true);
        setUpQueryExpectations("");
        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Test
    public void testListWithEveryonePermissions() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);

        setUpGetEntityExpectations(QueryType.GetAnyDbUserByUserId,
                                    IdQueryParameters.class,
                                    new String[] {"Id"},
                                    new Object[] {GUIDS[1]},
                                    getUserByIdx(1),
                                    true);
        setUpQueryExpectations("", null, EVERYONE);

        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }

    @Override
    protected Permission getModel() {
        Permission model = new Permission();
        model.setDataCenter(new DataCenter());
        model.getDataCenter().setId(GUIDS[2].toString());
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

