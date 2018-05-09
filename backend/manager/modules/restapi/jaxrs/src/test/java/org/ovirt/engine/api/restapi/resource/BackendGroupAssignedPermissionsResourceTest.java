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
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendGroupAssignedPermissionsResourceTest
        extends AbstractBackendAssignedPermissionsResourceTest {

    public BackendGroupAssignedPermissionsResourceTest() {
        super(GUIDS[1],
              QueryType.GetPermissionsByAdElementId,
              new IdQueryParameters(GUIDS[1]),
              Group.class,
              "Group.Id",
              "Id");
    }

    @Test
    public void testAddIncompletePermission() {
        Permission model = new Permission();
        model.setGroup(new Group());
        model.getGroup().setId(GUIDS[1].toString());
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
        assertTrue(model.isSetGroup());
        assertEquals(GUIDS[1].toString(), model.getGroup().getId());
    }

    @Override
    @Test
    public void testList() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        setUpGetEntityExpectations(QueryType.GetAnyDbUserByUserId,
                IdQueryParameters.class,
                new String[] {"Id"},
                new Object[] {GUIDS[1]},
                getUserByIdx(1));
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
                                    getUserByIdx(1));
        setUpQueryExpectations("", null, EVERYONE);

        collection.setUriInfo(uriInfo);
        verifyCollection(getCollection());
    }
}
