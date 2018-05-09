package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendPermitResourceTest extends AbstractBackendSubResourceTest<Permit, ActionGroup, BackendPermitResource> {

    private static final Guid ROLE_ID = new Guid("11111111-1111-1111-1111-111111111111");

    public BackendPermitResourceTest() {
        super(new BackendPermitResource("1", new BackendPermitsResource(ROLE_ID)));
    }

    @Test
    public void testGetBadId() {
        doTestGetNotFound("foo");
    }

    @Test
    public void testGetNotFound() {
        doTestGetNotFound("11111");
    }

    private void doTestGetNotFound(String id) {
        BackendPermitResource resource = new BackendPermitResource(id, new BackendPermitsResource(ROLE_ID));
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGet() {
        initResource(resource.parent);
        for (ActionGroup action : ActionGroup.values()) {
            resource.id = Integer.toString(action.getId());
            verifyPermit(resource.get(), action);
        }
        resource.id = "1"; // reset id, because 'resource' is used for multiple tests.
    }

    @Test
    public void testRemoveBadId() {
        doTestRemoveNotFound("foo");
    }

    @Test
    public void testRemoveNotFound() {
        doTestRemoveNotFound("11111");
    }

    private void doTestRemoveNotFound(String id) {
        initResource(resource.parent);
        resource.id = id;
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::remove));
        resource.id = "1";
    }

    @Test
    public void testRemove() {
        initResource(resource.parent);
        List<ActionGroup> actionGroups = new ArrayList<>();
        actionGroups.add(ActionGroup.forValue(1));
        setUriInfo(setUpActionExpectations(ActionType.DetachActionGroupsFromRole,
                ActionGroupsToRoleParameter.class,
                new String[] { "RoleId", "ActionGroups" },
                new Object[] { GUIDS[1], actionGroups },
                true,
                true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        initResource(resource.parent);
        List<ActionGroup> actionGroups = new ArrayList<>();
        actionGroups.add(ActionGroup.forValue(1));
        setUriInfo(setUpActionExpectations(ActionType.DetachActionGroupsFromRole,
                ActionGroupsToRoleParameter.class,
                new String[] { "RoleId", "ActionGroups" },
                new Object[] { GUIDS[1], actionGroups },
                valid,
                success));

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }
    private void verifyPermit(Permit permit, ActionGroup action) {
        assertEquals(Integer.toString(action.getId()), permit.getId());
        assertEquals(action.name().toLowerCase(), permit.getName());
        assertNotNull(permit.getRole());
        assertEquals(ROLE_ID.toString(), permit.getRole().getId());
    }

    @Override
    protected ActionGroup getEntity(int index) {
        // TODO Auto-generated method stub
        return null;
    }
}

