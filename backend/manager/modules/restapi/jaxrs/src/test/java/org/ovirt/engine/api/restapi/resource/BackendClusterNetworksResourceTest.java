package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterNetworksResourceTest
    extends AbstractBackendCollectionResourceTest<Network, org.ovirt.engine.core.common.businessentities.network.Network, BackendClusterNetworksResource> {

    private static final Guid CLUSTER_ID = GUIDS[1];
    private static final Guid DATA_CENTER_ID = GUIDS[2];

    public BackendClusterNetworksResourceTest() {
        super(new BackendClusterNetworksResource(CLUSTER_ID), null, null);
    }

    @Test
    public void testAddNetwork() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            VdcQueryType.GetClusterById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { CLUSTER_ID },
            getCluster(1)
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllNetworks,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DATA_CENTER_ID },
            getNetworks()
        );
        setUpActionExpectations(
            VdcActionType.AttachNetworkToCluster,
            AttachNetworkToClusterParameter.class,
            new String[] { "ClusterId" },
            new Object[] { CLUSTER_ID },
            true,
            true
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getEntity(0)
        );
        Network model = getModel(0);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Network);
        verifyModel((Network) response.getEntity(), 0);
    }

    @Test
    public void testAddNetworkCantDo() throws Exception {
        doTestBadAddNetwork(false, true, CANT_DO);
    }

    @Test
    public void testAddNetworkFailure() throws Exception {
        doTestBadAddNetwork(true, false, FAILURE);
    }

    private void doTestBadAddNetwork(boolean valid, boolean success, String detail) throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            VdcQueryType.GetClusterById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { CLUSTER_ID },
            getCluster(1)
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllNetworks,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DATA_CENTER_ID },
            getNetworks()
        );
        setUpActionExpectations(
            VdcActionType.AttachNetworkToCluster,
            AttachNetworkToClusterParameter.class,
            new String[] { "ClusterId" },
            new Object[] { CLUSTER_ID },
            valid,
            success
        );

        Network model = getModel(0);
        try {
            collection.add(model);
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddNameSuppliedButNoId() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            VdcQueryType.GetClusterById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { CLUSTER_ID },
            getCluster(1)
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllNetworks,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DATA_CENTER_ID },
            getNetworks()
        );
        setUpActionExpectations(
            VdcActionType.AttachNetworkToCluster,
            AttachNetworkToClusterParameter.class,
            new String[] { "ClusterId" },
            new Object[] { CLUSTER_ID },
            true,
            true
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getEntity(0)
        );

        Network model = new Network();
        model.setName(NAMES[0]);
        model.setDescription(DESCRIPTIONS[0]);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Network);
        verifyModel((Network) response.getEntity(), 0);
    }

    @Test
    public void testAddIdSuppliedButNoName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(
            VdcQueryType.GetClusterById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { CLUSTER_ID },
            getCluster(1)
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllNetworks,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DATA_CENTER_ID },
            getNetworks()
        );
        setUpActionExpectations(
            VdcActionType.AttachNetworkToCluster,
            AttachNetworkToClusterParameter.class,
            new String[] { "ClusterId" },
            new Object[] { CLUSTER_ID },
            true,
            true
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getEntity(0)
        );

        Network model = new Network();
        model.setId(GUIDS[0].toString());
        collection.add(model);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Network);
        verifyModel((Network) response.getEntity(), 0);
    }

    @Test
    public void testAddIncompleteParametersNoName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());

        Network model = new Network();
        model.setDescription(DESCRIPTIONS[0]);
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        }
        catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Network", "add", "id|name");
        }
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetClusterById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { CLUSTER_ID },
            getCluster(0)
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllNetworks,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DATA_CENTER_ID },
            getNetworks(),
            failure
        );
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.network.Network getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.network.Network entity = mock(
            org.ovirt.engine.core.common.businessentities.network.Network.class
        );
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        return entity;
    }

    @Override
    protected List<Network> getCollection() {
        return collection.list().getNetworks();
    }

    private org.ovirt.engine.core.common.businessentities.Cluster getCluster(int index) {
        org.ovirt.engine.core.common.businessentities.Cluster entity = mock(
            org.ovirt.engine.core.common.businessentities.Cluster.class
        );
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getStoragePoolId()).thenReturn(DATA_CENTER_ID);
        return entity;
    }

    private Network getModel(int index) {
        Network model = new Network();
        model.setId(GUIDS[0].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    private List<org.ovirt.engine.core.common.businessentities.network.Network> getNetworks() {
        List<org.ovirt.engine.core.common.businessentities.network.Network> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }
}
