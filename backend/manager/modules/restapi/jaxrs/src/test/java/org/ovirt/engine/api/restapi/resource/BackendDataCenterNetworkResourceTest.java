package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDataCenterNetworkResourceTest
    extends AbstractBackendSubResourceTest<Network, org.ovirt.engine.core.common.businessentities.network.Network, BackendDataCenterNetworkResource> {

    private static final Guid DATA_CENTER_ID = GUIDS[1];
    private static final Guid NETWORK_ID = GUIDS[0];

    public BackendDataCenterNetworkResourceTest() {
        super(new BackendDataCenterNetworkResource(NETWORK_ID.toString()));
    }

    @Test
    public void testBadGuid() throws Exception {
        try {
            new BackendDataCenterNetworkResource("foo");
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            null
        );

        try {
            resource.get();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            getEntity(0)
        );

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            null,
            null
        );

        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            getEntity(0)
        );
        setUpActionExpectations(
            VdcActionType.UpdateNetwork,
            AddNetworkStoragePoolParameters.class,
            new String[] { "StoragePoolId" },
            new Object[] { DATA_CENTER_ID },
            true,
            true
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            getEntity(0)
        );

        verifyModel(resource.update(getModel(0)), 0);
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            getEntity(0)
        );
        setUpActionExpectations(
            VdcActionType.UpdateNetwork,
            AddNetworkStoragePoolParameters.class,
            new String[] { "StoragePoolId" },
            new Object[] { DATA_CENTER_ID },
            valid,
            success
        );

        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            getEntity(0)
        );

        Network model = getModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    @Test
    public void testRemoveNotFound() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            null,
            null
        );

        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            getEntity(0)
        );
        setUpActionExpectations(
            VdcActionType.RemoveNetwork,
            RemoveNetworkParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            true,
            true
        );

        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            null,
            null
        );

        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
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
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            getEntity(0)
        );
        setUpActionExpectations(
            VdcActionType.RemoveNetwork,
            RemoveNetworkParameters.class,
            new String[] { "Id" },
            new Object[] {NETWORK_ID},
            valid,
            success
        );
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.network.Network getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.network.Network entity = mock(
            org.ovirt.engine.core.common.businessentities.network.Network.class
        );
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getDataCenterId()).thenReturn(GUIDS[1]);
        return entity;
    }

    private static Network getModel(int index) {
        Network model = new Network();
        model.setId(GUIDS[0].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }
}

