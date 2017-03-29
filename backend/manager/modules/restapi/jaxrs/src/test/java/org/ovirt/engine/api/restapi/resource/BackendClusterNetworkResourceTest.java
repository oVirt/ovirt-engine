package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.NetworkCluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterNetworkResourceTest
    extends AbstractBackendSubResourceTest<Network, org.ovirt.engine.core.common.businessentities.network.Network, BackendClusterNetworkResource> {

    private static final Guid NETWORK_ID = GUIDS[0];
    private static final Guid CLUSTER_ID = GUIDS[1];

    public BackendClusterNetworkResourceTest() {
        super(new BackendClusterNetworkResource(CLUSTER_ID, NETWORK_ID.toString()));
    }

    @Test
    public void testBadGuid() throws Exception {
        try {
            new BackendClusterNetworkResource(CLUSTER_ID, "foo");
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
            null,
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
    public void testUpdate() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            getEntity(0)
        );
        setUpActionExpectations(
            VdcActionType.UpdateNetworkOnCluster,
            NetworkClusterParameters.class,
            new String[] {},
            new Object[] {},
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
        setUpEntityQueryExpectations(
            VdcQueryType.GetClusterById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { CLUSTER_ID },
            getCluster(1)
        );
        setUpActionExpectations(
            VdcActionType.DetachNetworkToCluster,
            AttachNetworkToClusterParameter.class,
            new String[] { "ClusterId" },
            new Object[] { CLUSTER_ID },
            true,
            true
        );
        verifyRemove(resource.remove());
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
        setUpEntityQueryExpectations(
            VdcQueryType.GetClusterById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { CLUSTER_ID },
            getCluster(1)
        );
        setUpActionExpectations(
            VdcActionType.DetachNetworkToCluster,
            AttachNetworkToClusterParameter.class,
            new String[] { "ClusterId" },
            new Object[] { CLUSTER_ID },
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
        NetworkCluster networkCluster = mock(NetworkCluster.class);
        org.ovirt.engine.core.common.businessentities.network.Network entity = mock(
            org.ovirt.engine.core.common.businessentities.network.Network.class
        );
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getCluster()).thenReturn(networkCluster);
        return entity;
    }

    private org.ovirt.engine.core.common.businessentities.Cluster getCluster(int index) {
        org.ovirt.engine.core.common.businessentities.Cluster entity = mock(
            org.ovirt.engine.core.common.businessentities.Cluster.class
        );
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        return entity;
    }

    private Network getModel(int i) {
        Network network = new Network();
        network.setId(GUIDS[i].toString());
        network.setName(NAMES[i]);
        network.setDisplay(true);
        return network;
    }
}

