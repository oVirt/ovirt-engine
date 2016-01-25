/*
Copyright (c) 2015 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.restapi.resource;

import org.junit.Test;
import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.model.WatchdogAction;
import org.ovirt.engine.api.model.WatchdogModel;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendVmWatchdogResourceTest
        extends AbstractBackendSubResourceTest<Watchdog, VmWatchdog, BackendVmWatchdogResource> {

    private static final Guid VM_ID = GUIDS[1];
    private static final Guid WATCHDOG_ID = GUIDS[0];

    public BackendVmWatchdogResourceTest() {
        super(new BackendVmWatchdogResource(WATCHDOG_ID.toString(), VM_ID));
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1);
        control.replay();
        Watchdog watchdog = resource.get();
        verifyModel(watchdog);
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
        Watchdog wd = resource.update(getUpdate());
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
                getEntity()
            );
        }
    }

    private VmWatchdog getEntity() {
        VmWatchdog watchdog = new VmWatchdog();
        watchdog.setId(WATCHDOG_ID);
        watchdog.setAction(VmWatchdogAction.RESET);
        watchdog.setModel(VmWatchdogType.i6300esb);
        return watchdog;
    }

    private Watchdog getUpdate() {
        Watchdog watchdog = new Watchdog();
        watchdog.setId(WATCHDOG_ID.toString());
        watchdog.setAction(WatchdogAction.RESET);
        watchdog.setModel(WatchdogModel.I6300ESB);
        return watchdog;
    }

    private void verifyModel(Watchdog model) {
        assertEquals(WATCHDOG_ID.toString(), model.getId());
        verifyLinks(model);
    }
}
