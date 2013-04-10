package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.api.model.WatchDog;
import org.ovirt.engine.api.model.WatchDogs;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResource.ParametersProvider;
import org.ovirt.engine.api.restapi.types.WatchdogAction;
import org.ovirt.engine.api.restapi.types.WatchdogModel;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendWatchdogResourceTest extends AbstractBackendSubResourceTest<WatchDog, VmWatchdog, BackendDeviceResource<WatchDog, WatchDogs, VmWatchdog>> {

    protected static BackendWatchdogsResource collection = getCollection();

    public BackendWatchdogResourceTest() {
        super(getResource());
    }

    protected void init() {
        super.init();
        initResource(resource.getCollection());
    }

    private static BackendDeviceResource<WatchDog, WatchDogs, VmWatchdog> getResource() {
        return new BackendWatchdogResource(GUIDS[1],
                collection,
                VdcActionType.UpdateWatchdog,
                getUpdateParams(),
                new String[] { "action", "model" });
    }

    private static ParametersProvider<WatchDog, VmWatchdog> getUpdateParams() {
        return new ParametersProvider<WatchDog, VmWatchdog>(){

            public VdcActionParametersBase getParameters(WatchDog model, VmWatchdog entity) {
                WatchdogParameters params = new WatchdogParameters();
                params.setModel(VmWatchdogType.getByName(model.getModel()));
                params.setAction(VmWatchdogAction.getByName(model.getAction()));
                params.setId(GUIDS[1]);
                params.setVm(true);
                return params;
            }};
    }

    @Override
    protected VmWatchdog getEntity(int index) {
        VmWatchdog wd = new VmWatchdog();
        wd.setId(GUIDS[1]);
        wd.setAction(VmWatchdogAction.RESET);
        wd.setModel(VmWatchdogType.i6300esb);
        return wd;
    }

    protected BackendDeviceResource<WatchDog, WatchDogs, VmWatchdog> getNotFoundResource() {
        BackendDeviceResource<WatchDog, WatchDogs, VmWatchdog> ret = getResource(GUIDS[2]);
        ret.setUriInfo(setUpBasicUriExpectations());
        initResource(ret);
        initResource(ret.getCollection());
        return ret;
    }

    private BackendDeviceResource<WatchDog, WatchDogs, VmWatchdog> getResource(Guid guid) {
        return new BackendWatchdogResource(guid, getCollection(), VdcActionType.UpdateWatchdog, null, new String[] {});
    }

    private static BackendWatchdogsResource getCollection() {
        return new BackendWatchdogsResource(GUIDS[0], VdcQueryType.GetWatchdog, new IdQueryParameters(GUIDS[1]));
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        control.replay();

        WatchDog watchDog = resource.get();
        verifyModel(watchDog, 1);
    }

    @Override
    protected void verifyModel(WatchDog model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        // wd has no name and description
        verifyLinks(model);
    }

    private void setUpEntityQueryExpectations(int cnt) throws Exception {
        for (int i = 0; i < cnt; i++) {
            setUpGetEntityExpectations(VdcQueryType.GetWatchdog,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[1] },
                    getEntity(0));
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpEntityQueryExpectations(2);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateWatchdog,
                WatchdogParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[1] },
                true,
                true));

        WatchDog wd = resource.update(getUpdate());
        assertTrue(wd.isSetAction());
    }

    private WatchDog getUpdate() {
        WatchDog watchDog = new WatchDog();
        watchDog.setAction(WatchdogAction.RESET.name().toLowerCase());
        watchDog.setModel(WatchdogModel.I6300ESB.name().toLowerCase());
        watchDog.setId(GUIDS[1].toString());
        return watchDog;
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        control.replay();

        WatchDog watchDog = resource.get();
        verifyModel(watchDog, 1);
    }

}
