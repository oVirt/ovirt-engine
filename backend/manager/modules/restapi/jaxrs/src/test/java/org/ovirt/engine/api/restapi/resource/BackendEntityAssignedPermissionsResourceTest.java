package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Group;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.Role;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

public abstract class BackendEntityAssignedPermissionsResourceTest
        extends AbstractBackendAssignedPermissionsResourceTest {

    private Guid targetId;

    /**
     * This constructor is intended for tests that check permissions assigned to a generic type of entity, thus it
     * needs the type and identifier of one of those entities. For example, a test intended to check permissions on
     * the {@code System} entity will pass {@code Guid.SYSTEM} as identifier and {@code BaseResource.class} as type.
     *
     * @param targetId the identifier of the entity
     * @param targetType the type of the entity
     */
    protected BackendEntityAssignedPermissionsResourceTest(Guid targetId, Class<? extends BaseResource> targetType) {
        super(
            targetId,
            QueryType.GetPermissionsForObject,
            new GetPermissionsForObjectParameters(GUIDS[1]),
            targetType,
            "User.Id",
            "ObjectId"
        );
        this.targetId = targetId;
    }

    public BackendEntityAssignedPermissionsResourceTest() {
        // The concrete tests in this class always use a fixed data center as the entity:
        this(GUIDS[2], DataCenter.class);
    }

    @Test
    public void testAddIncompletePermission() {
        Permission model = new Permission();
        model.setDataCenter(new DataCenter());
        model.getDataCenter().setId(GUIDS[2].toString());
        model.setRole(new Role());
        model.getRole().setId(GUIDS[3].toString());

        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "Permission", "add", "user|group.id");
    }

    @Test
    public void testAddGroupPermission() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.AddPermission,
                                  PermissionsOperationsParameters.class,
                                  new String[] { "Group.Id",
                                                 "Permission.AdElementId",
                                                 "Permission.ObjectId",
                                                 "Permission.RoleId" },
                                  new Object[] { GUIDS[1], GUIDS[1], targetId, GUIDS[3] },
                                  true,
                                  true,
                                  GUIDS[0],
                                  QueryType.GetPermissionById,
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

