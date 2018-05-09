package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendClusterNetworksResourceTest extends AbstractBackendNetworksResourceTest<BackendClusterNetworksResource> {

    static final Guid CLUSTER_ID = GUIDS[1];

    public BackendClusterNetworksResourceTest() {
        super(new BackendClusterNetworksResource(CLUSTER_ID.toString()));
    }

    @Test
    public void testAddNetwork() {
        setUpClusterExpectations(CLUSTER_ID);

        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, null);
        setUpGetNetworksByDataCenterExpectations(1, null);
        setUpActionExpectations(ActionType.AttachNetworkToCluster,
                AttachNetworkToClusterParameter.class,
                new String[] { "ClusterId" },
                new Object[] { CLUSTER_ID },
                true,
                true);
        Network model = getModel(0);
        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Network);
        verifyModel((Network) response.getEntity(), 0);
    }

    @Test
    public void testAddNetworkCantDo() {
        doTestBadAddNetwork(false, true, CANT_DO);
    }

    @Test
    public void testAddNetworkFailure() {
        doTestBadAddNetwork(true, false, FAILURE);
    }

    private void doTestBadAddNetwork(boolean valid, boolean success, String detail) {
        setUpClusterExpectations(CLUSTER_ID);

        setUriInfo(setUpBasicUriExpectations());
        setUpGetNetworksByDataCenterExpectations(1, null);
        setUpActionExpectations(ActionType.AttachNetworkToCluster,
                AttachNetworkToClusterParameter.class,
                new String[] { "ClusterId" },
                new Object[] { CLUSTER_ID },
                valid,
                success);
        Network model = getModel(0);

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddNameSuppliedButNoId() {
        setUriInfo(setUpBasicUriExpectations());
        Network model = new Network();
        model.setName("orcus");
        model.setDescription(DESCRIPTIONS[0]);
        setUpEntityQueryExpectations(1, null);
        setUpGetNetworksByDataCenterExpectations(1, null);
        setUpClusterExpectations(CLUSTER_ID);
        setUpActionExpectations(ActionType.AttachNetworkToCluster,
                AttachNetworkToClusterParameter.class,
                new String[] { "ClusterId" },
                new Object[] { CLUSTER_ID },
                true,
                true);
        collection.add(model);
    }

    @Test
    public void testAddIdSuppliedButNoName() {
        setUriInfo(setUpBasicUriExpectations());
        Network model = new Network();
        model.setId("11111111-1111-1111-1111-111111111111");
        setUpEntityQueryExpectations(1, null);
        setUpGetNetworksByDataCenterExpectations(1, null);
        setUpClusterExpectations(CLUSTER_ID);
        setUpActionExpectations(ActionType.AttachNetworkToCluster,
                AttachNetworkToClusterParameter.class,
                new String[] { "ClusterId" },
                new Object[] { CLUSTER_ID },
                true,
                true);
        collection.add(model);
    }

    @Test
    public void testAddIncompleteParametersNoName() {
        Network model = new Network();
        model.setDescription(DESCRIPTIONS[0]);
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)), "Network", "add", "id|name");
    }

    @Override
    protected void setUpEntityQueryExpectations(int times, Object failure) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(QueryType.GetAllNetworksByClusterId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { CLUSTER_ID },
                                         getEntityList(),
                                         failure);
        }
    }

    protected Cluster setUpClusterExpectations(Guid id) {
        Cluster group = mock(Cluster.class);
        when(group.getId()).thenReturn(id);
        when(group.getStoragePoolId()).thenReturn(GUIDS[2]);

        setUpEntityQueryExpectations(QueryType.GetClusterById,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { id },
                                     group);
        return group;
    }

    protected void setUpGetNetworksByDataCenterExpectations(int times, Object failure) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(QueryType.GetAllNetworks,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { GUIDS[2] },
                                         getEntityList(),
                                         failure);
        }
    }

    @Override
    protected void setUpQueryExpectations(String query) {
        setUpEntityQueryExpectations(1);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(1, failure);
    }
}
