package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.api.model.WatchDog;
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

public class BackendWatchdogResourceTest extends AbstractBackendSubResourceTest<WatchDog, VmWatchdog, BackendWatchdogResource> {

    private static final Guid VM_ID = GUIDS[1];
    private static final Guid WATCHDOG_ID = GUIDS[0];

    private static BackendWatchdogResource getResource() {
        return new BackendWatchdogResource(
            true,
            VM_ID,
            WATCHDOG_ID,
            getCollection(),
            VdcActionType.UpdateWatchdog,
            getUpdateParams(),
            new String[] { "action", "model" }
        );
    }

    private static BackendWatchdogsResource getCollection() {
        return new BackendWatchdogsResource(true, VM_ID, VdcQueryType.GetWatchdog, new IdQueryParameters(VM_ID));
    }

    private static ParametersProvider<WatchDog, VmWatchdog> getUpdateParams() {
        return new ParametersProvider<WatchDog, VmWatchdog>() {
            public VdcActionParametersBase getParameters(WatchDog model, VmWatchdog entity) {
                WatchdogParameters params = new WatchdogParameters();
                params.setModel(VmWatchdogType.getByName(model.getModel()));
                params.setAction(VmWatchdogAction.getByName(model.getAction()));
                params.setId(VM_ID);
                params.setVm(true);
                return params;
            }
        };
    }

    public BackendWatchdogResourceTest() {
        super(getResource());
    }

    protected void init() {
        super.init();
        initResource(resource.getCollection());
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        control.replay();
        WatchDog watchDog = resource.get();
        verifyModel(watchDog, 0);
    }

    @Override
    protected void verifyModel(WatchDog model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        // wd has no name and description
        verifyLinks(model);
    }

    @Test
    public void testUpdate() throws Exception {
        setUpEntityQueryExpectations(2);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.UpdateWatchdog,
                WatchdogParameters.class,
                new String[] { "Id" },
                new Object[] { VM_ID },
                true,
                true
            )
        );
        WatchDog wd = resource.update(getUpdate());
        assertTrue(wd.isSetAction());
    }

    @Test
    public void testRemove() throws Exception {
        setUpEntityQueryExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.RemoveWatchdog,
                WatchdogParameters.class,
                new String[]{"Id"},
                new Object[]{VM_ID},
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    private void setUpEntityQueryExpectations(int cnt) throws Exception {
        for (int i = 0; i < cnt; i++) {
            setUpGetEntityExpectations(
                VdcQueryType.GetWatchdog,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { VM_ID },
                getWatchdog()
            );
        }
    }

    private VmWatchdog getWatchdog() {
        VmWatchdog watchDog = new VmWatchdog();
        watchDog.setId(WATCHDOG_ID);
        watchDog.setAction(VmWatchdogAction.RESET);
        watchDog.setModel(VmWatchdogType.i6300esb);
        return watchDog;
    }

    private WatchDog getUpdate() {
        WatchDog watchDog = new WatchDog();
        watchDog.setId(WATCHDOG_ID.toString());
        watchDog.setAction(WatchdogAction.RESET.name().toLowerCase());
        watchDog.setModel(WatchdogModel.I6300ESB.name().toLowerCase());
        return watchDog;
    }
}
