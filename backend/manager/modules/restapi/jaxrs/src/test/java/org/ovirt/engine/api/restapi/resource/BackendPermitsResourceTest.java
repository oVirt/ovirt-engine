package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Permit;
import org.ovirt.engine.core.common.action.ActionGroupsToRoleParameter;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendPermitsResourceTest extends AbstractBackendCollectionResourceTest<Permit, ActionGroup, BackendPermitsResource> {

    public BackendPermitsResourceTest() {
        super(new BackendPermitsResource(GUIDS[1]), null, "");
    }

    @Test
    @Disabled
    @Override
    public void testQuery() {
    }

    @Test
    public void testAddPermit() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.AttachActionGroupsToRole,
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
    protected void setUpQueryExpectations(String query, Object failure) {
        assertEquals("", query);

        setUpEntityQueryExpectations(QueryType.GetRoleActionGroupsByRoleId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                                     setUpActionGroups(),
                                     failure);

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
