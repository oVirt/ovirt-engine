/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Watchdog;
import org.ovirt.engine.api.model.WatchdogAction;
import org.ovirt.engine.api.model.WatchdogModel;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.WatchdogParameters;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.businessentities.VmWatchdogAction;
import org.ovirt.engine.core.common.businessentities.VmWatchdogType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendVmWatchdogsResourceTest
        extends AbstractBackendCollectionResourceTest<Watchdog, VmWatchdog, BackendVmWatchdogsResource> {

    private static final Guid VM_ID = GUIDS[1];
    private static final Guid WATCHDOG_ID = GUIDS[0];

    public BackendVmWatchdogsResourceTest() {
        super(new BackendVmWatchdogsResource(VM_ID), null, null);
    }

    @Override
    protected List<Watchdog> getCollection() {
        return collection.list().getWatchdogs();
    }

    @Test
    public void testAdd() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(
            ActionType.AddWatchdog,
            WatchdogParameters.class,
            new String[] {},
            new Object[] {},
            true,
            true,
            WATCHDOG_ID,
            QueryType.GetWatchdog,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            getEntity()
        );
        Watchdog model = getModel();
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Watchdog);
        verifyModel((Watchdog) response.getEntity());
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(
            QueryType.GetWatchdog,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { VM_ID },
            getEntities(),
            failure
        );
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
    protected void verifyCollection(List<Watchdog> collection) {
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
