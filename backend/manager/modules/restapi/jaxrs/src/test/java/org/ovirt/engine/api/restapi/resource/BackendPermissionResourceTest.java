package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Permission;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.PermissionsOperationsParameters;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.GetPermissionsForObjectParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendPermissionResourceTest
        extends AbstractBackendSubResourceTest<
                        Permission,
                        org.ovirt.engine.core.common.businessentities.Permission,
                        BackendPermissionResource> {

    public BackendPermissionResourceTest() {
        super(new BackendPermissionResource(GUIDS[0].toString(),
                                            GUIDS[1],
                                            new BackendAssignedPermissionsResource(GUIDS[0],
                                                                                   QueryType.GetPermissionsForObject,
                                                                                   new GetPermissionsForObjectParameters(GUIDS[0]),
                                                                                   Cluster.class,
                                                                                   VdcObjectType.Cluster),
                                            User.class));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        initResource(resource.parent);
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(
                WebApplicationException.class, () -> new BackendPermissionResource("foo", null, null, null)));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(QueryType.GetAllDbUsers,
                QueryParametersBase.class,
                new String[] { "Refresh", "Filtered" },
                new Object[] { false, false },
                getUsers());

        setUpGetEntityExpectations(1);

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testRemove() {
        setUpGetEntityExpectations(2);
        setUpEntityQueryExpectations(
            QueryType.GetAllDbUsers,
            QueryParametersBase.class,
            new String[] { "Refresh", "Filtered" },
            new Object[] { false, false },
            getUsers()
        );
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemovePermission,
                PermissionsOperationsParameters.class,
                new String[] { "Permission.Id" },
                new Object[] { GUIDS[0] },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() {
        setUpEntityQueryExpectations(
            QueryType.GetAllDbUsers,
            QueryParametersBase.class,
            new String[] {},
            new Object[] {},
            getUsers()
        );
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        setUpEntityQueryExpectations(
            QueryType.GetAllDbUsers,
            QueryParametersBase.class,
            new String[] {},
            new Object[] {},
            getUsers()
        );
        doTestBadRemove(true, false, FAILURE);
    }

    @Test
    public void testRemoveNonExistant() {
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations(2);
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemovePermission,
                PermissionsOperationsParameters.class,
                new String[] { "Permission.Id" },
                new Object[] { GUIDS[0] },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    protected void setUpGetEntityExpectations(int times) {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) {
        for (int i = 0; i < times; i++) {
            setUpGetEntityExpectations(
                QueryType.GetPermissionById,
                IdQueryParameters.class,
                new String[]{"Id"},
                new Object[]{GUIDS[0]},
                notFound ? null : getEntity(0)
            );
        }
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.Permission getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.Permission permission =
                new org.ovirt.engine.core.common.businessentities.Permission();
        permission.setId(GUIDS[0]);
        permission.setAdElementId(GUIDS[1]);
        permission.setRoleId(GUIDS[2]);
        return permission;
    }

    @Override
    protected void verifyModel(Permission model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertTrue(model.isSetUser());
        assertEquals(GUIDS[1].toString(), model.getUser().getId());
        assertTrue(model.isSetRole());
        assertEquals(GUIDS[2].toString(), model.getRole().getId());
    }

    protected ArrayList<DbUser> getUsers() {
        ArrayList<DbUser> users = new ArrayList<>();
        for (int i=0; i < NAMES.length; i++) {
            DbUser user = new DbUser();
            user.setId(GUIDS[i]);
            user.setLoginName(NAMES[i]);
            users.add(user);
        }
        return users;
    }
}

