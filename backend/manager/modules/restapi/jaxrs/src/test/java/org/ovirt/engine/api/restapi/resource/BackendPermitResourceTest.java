package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.compat.Guid;

public class BackendPermitResourceTest extends AbstractBackendSubResourceTest<Permit, ActionGroup, BackendPermitResource> {

    private static final Guid ROLE_ID = new Guid("11111111-1111-1111-1111-111111111111");

    public BackendPermitResourceTest() {
        super(new BackendPermitResource("1", new BackendPermitsResource(ROLE_ID)));
    }

    @Test
    public void testGetBadId() throws Exception {
        doTestGetNotFound("foo");
    }

    @Test
    public void testGetNotFound() throws Exception {
        doTestGetNotFound("11111");
    }

    private void doTestGetNotFound(String id) throws Exception {
        BackendPermitResource resource = new BackendPermitResource(id, new BackendPermitsResource(ROLE_ID));
        resource.getParent().setMappingLocator(mapperLocator);
        try {
            control.replay();
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    @Test
    public void testGet() {
        initResource(resource.parent);
        control.replay();
        for (ActionGroup action : ActionGroup.values()) {
            resource.id = Integer.toString(action.getId());
            verifyPermit(resource.get(), action);
        }
        resource.id = "1"; // reset id, because 'resource' is used for multiple tests.
    }

    @Test
    public void testRemoveBadId() throws Exception {
        doTestRemoveNotFound("foo");
    }

    @Test
    public void testRemoveNotFound() throws Exception {
        doTestRemoveNotFound("11111");
    }

    private void doTestRemoveNotFound(String id) throws Exception {
        initResource(resource.parent);
        resource.id = id;
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
        resource.id = "1";
    }

    @Test
    public void testRemove() throws Exception {
        initResource(resource.parent);
        List<ActionGroup> actionGroups = new ArrayList<>();
        actionGroups.add(ActionGroup.forValue(1));
        setUriInfo(setUpActionExpectations(VdcActionType.DetachActionGroupsFromRole,
                ActionGroupsToRoleParameter.class,
                new String[] { "RoleId", "ActionGroups" },
                new Object[] { GUIDS[1], actionGroups },
                true,
                true));
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        initResource(resource.parent);
        List<ActionGroup> actionGroups = new ArrayList<>();
        actionGroups.add(ActionGroup.forValue(1));
        setUriInfo(setUpActionExpectations(VdcActionType.DetachActionGroupsFromRole,
                ActionGroupsToRoleParameter.class,
                new String[] { "RoleId", "ActionGroups" },
                new Object[] { GUIDS[1], actionGroups },
                valid,
                success));
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
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

