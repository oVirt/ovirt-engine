package org.ovirt.engine.api.restapi.resource;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

import org.junit.Ignore;
import org.junit.Test;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendPermitsResourceTest extends AbstractBackendCollectionResourceTest<Permit, ActionGroup, BackendPermitsResource> {

    public BackendPermitsResourceTest() {
        super(new BackendPermitsResource(GUIDS[1]), null, "");
    }

    @Test
    @Ignore
    @Override
    public void testQuery() throws Exception {
    }

    @Test
    public void testAddPermit() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AttachActionGroupsToRole,
                                  ActionGroupsToRoleParameter.class,
                                  new String[] { "RoleId" },
                                  new Object[] { GUIDS[1] },
                                  true,
                                  true,
                                  GUIDS[2],
                                  null,
                                  null,
                                  null,
                                  null,
                                  getEntity(1));

        Permit model = new Permit();
        model.setId("1");

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Permit);
        verifyModel((Permit)response.getEntity(), 0);
    }

    @Override
    protected List<Permit> getCollection() {
        return collection.list().getPermits();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        assertEquals("", query);

        setUpEntityQueryExpectations(VdcQueryType.GetRoleActionGroupsByRoleId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                                     setUpActionGroups(),
                                     failure);

        control.replay();
    }

    static List<ActionGroup> setUpActionGroups() {
        List<ActionGroup> actionGroups = new ArrayList<>();
        for (int i = 1; i <= NAMES.length; i++) {
            actionGroups.add(ActionGroup.forValue(i));
        }
        return actionGroups;
    }

    @Override
    protected ActionGroup getEntity(int index) {
        return ActionGroup.forValue(index);
    }

    @Override
    protected void verifyModel(Permit model, int index) {
        assertEquals(Integer.toString(index + 1), model.getId());
        ActionGroup actionGroup = ActionGroup.forValue(index + 1);
        assertEquals(actionGroup.name().toLowerCase(), model.getName());
    }

}
