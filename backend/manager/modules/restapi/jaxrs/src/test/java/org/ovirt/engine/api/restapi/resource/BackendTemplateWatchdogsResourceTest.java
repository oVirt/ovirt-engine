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

import java.util.Collections;
import java.util.List;
import javax.ws.rs.core.Response;

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

public class BackendTemplateWatchdogsResourceTest
        extends AbstractBackendCollectionResourceTest<Watchdog, VmWatchdog, BackendInstanceTypeWatchdogsResource> {

    private static final Guid INSTANCE_TYPE_ID = GUIDS[1];
    private static final Guid WATCHDOG_ID = GUIDS[0];

    public BackendTemplateWatchdogsResourceTest() {
        super(new BackendInstanceTypeWatchdogsResource(INSTANCE_TYPE_ID), null, null);
    }

    @Override
    protected List<Watchdog> getCollection() {
        return collection.list().getWatchdogs();
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
            WATCHDOG_ID,
            VdcQueryType.GetWatchdog,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { INSTANCE_TYPE_ID },
            getEntity()
        );
        Watchdog model = getModel();
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Watchdog);
        verifyModel((Watchdog) response.getEntity());
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetWatchdog,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { INSTANCE_TYPE_ID },
            getEntities(),
            failure
        );
        control.replay();
    }

    private List<VmWatchdog> getEntities() {
        return Collections.singletonList(getEntity());
    }

    private VmWatchdog getEntity() {
        VmWatchdog watchdog = new VmWatchdog();
        watchdog.setId(WATCHDOG_ID);
        watchdog.setAction(VmWatchdogAction.RESET);
        watchdog.setModel(VmWatchdogType.i6300esb);
        return watchdog;
    }

    private Watchdog getModel() {
        Watchdog watchDog = new Watchdog();
        watchDog.setAction(WatchdogAction.RESET);
        watchDog.setModel(WatchdogModel.I6300ESB);
        watchDog.setId(WATCHDOG_ID.toString());
        return watchDog;
    }

    @Override
    protected void verifyCollection(List<Watchdog> collection) throws Exception {
        assertNotNull(collection);
        assertEquals(1, collection.size());
        if(!collection.isEmpty()) {
            verifyModel(collection.get(0));
        }
    }

    private void verifyModel(Watchdog model) {
        assertEquals(WATCHDOG_ID.toString(), model.getId());
        verifyLinks(model);
    }
}
