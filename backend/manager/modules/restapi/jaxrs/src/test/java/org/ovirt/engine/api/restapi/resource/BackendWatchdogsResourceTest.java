package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.model.WatchdogAction;
import org.ovirt.engine.api.model.WatchdogModel;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendWatchdogsResourceTest extends AbstractBackendWatchdogsResourceTest<BackendWatchdogsResource> {

    public BackendWatchdogsResourceTest() {
        super(new BackendWatchdogsResource(true, PARENT_ID, VdcQueryType.GetWatchdog, new IdQueryParameters(PARENT_ID)));
    }

    @Test
    public void testAdd() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(
            VdcActionType.AddWatchdog,
            WatchdogParameters.class,
            new String[] {},
            new Object[] {},
            true,
            true,
            null,
            VdcQueryType.GetWatchdog,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { PARENT_ID },
            getEntity(0)
        );
        Watchdog model = getModel(0);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Watchdog);
        verifyModel((Watchdog) response.getEntity(), 0);
    }

    private Watchdog getModel(int i) {
        Watchdog watchDog = new Watchdog();
        watchDog.setAction(WatchdogAction.RESET.name().toLowerCase());
        watchDog.setModel(WatchdogModel.I6300ESB.name().toLowerCase());
        watchDog.setId(GUIDS[i].toString());
        return watchDog;
    }
}
