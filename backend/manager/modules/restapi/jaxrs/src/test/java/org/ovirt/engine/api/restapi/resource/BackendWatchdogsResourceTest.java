package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.VM;
import org.ovirt.engine.api.model.WatchDog;
import org.ovirt.engine.api.model.WatchDogs;
import org.ovirt.engine.api.model.WatchdogAction;
import org.ovirt.engine.api.model.WatchdogModel;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendWatchdogsResourceTest extends AbstractBackendWatchdogsResourceTest<BackendWatchdogsResource> {

    public BackendWatchdogsResourceTest() {
        super(new BackendWatchdogsResource(PARENT_ID, VdcQueryType.GetWatchdog, new IdQueryParameters(PARENT_ID)));
    }

    @Test
    public void testRemove() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetWatchdog,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { PARENT_ID },
                getEntity(0),
                null);
        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { PARENT_ID },
                new VM(),
                null);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveWatchdog,
                WatchdogParameters.class,
                new String[] { "Id" },
                new Object[] { PARENT_ID },
                true,
                true));
        Response remove = collection.deprecatedRemove(GUIDS[0].toString());
        verifyRemove(remove);
    }

    BackendDeviceResource<WatchDog, WatchDogs, VmWatchdog> getNotFoundResource() {
        return new BackendWatchdogResource(new Guid("0d0264ef-40de-45a1-b746-83a0088b47a7"),
                collection,
                VdcActionType.UpdateWatchdog,
                getUpdateProvider(),
                new String[] {});
    }

    private ParametersProvider<WatchDog, VmWatchdog> getUpdateProvider() {
        return new ParametersProvider<WatchDog, VmWatchdog>() {

            @Override
            public VdcActionParametersBase getParameters(WatchDog model, VmWatchdog entity) {
                WatchdogParameters watchdogParameters = new WatchdogParameters();
                watchdogParameters.setAction(VmWatchdogAction.getByName(model.getAction()));
                watchdogParameters.setModel(VmWatchdogType.getByName(model.getModel()));
                watchdogParameters.setVm(true);
                watchdogParameters.setId(PARENT_ID);
                return watchdogParameters;
            }
        };
    }

    @Test
    public void testAdd() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetVmByVmId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { PARENT_ID },
                getEntity(0),
                1);

        setUpCreationExpectations(VdcActionType.AddWatchdog,
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
                getEntity(0));
        WatchDog model = getModel(0);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof WatchDog);
        verifyModel((WatchDog) response.getEntity(), 0);

    }

    private WatchDog getModel(int i) {
        WatchDog watchDog = new WatchDog();
        watchDog.setAction(WatchdogAction.RESET.name().toLowerCase());
        watchDog.setModel(WatchdogModel.I6300ESB.name().toLowerCase());
        watchDog.setId(PARENT_ID.toString());
        return watchDog;
    }

}
