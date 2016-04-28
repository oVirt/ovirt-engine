package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterNetworksResourceTest extends AbstractBackendNetworksResourceTest<BackendClusterNetworksResource> {

    static final Guid CLUSTER_ID = GUIDS[1];

    public BackendClusterNetworksResourceTest() {
        super(new BackendClusterNetworksResource(CLUSTER_ID.toString()));
    }

    @Test
    public void testAddNetwork() throws Exception {
        setUpClusterExpectations(CLUSTER_ID);

        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, null);
        setUpGetClusterExpectations(1);
        setUpGetNetworksByDataCenterExpectations(1, null);
        setUpActionExpectations(VdcActionType.AttachNetworkToCluster,
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
    public void testAddNetworkCantDo() throws Exception {
        doTestBadAddNetwork(false, true, CANT_DO);
    }

    @Test
    public void testAddNetworkFailure() throws Exception {
        doTestBadAddNetwork(true, false, FAILURE);
    }

    private void doTestBadAddNetwork(boolean valid, boolean success, String detail) throws Exception {
        setUpClusterExpectations(CLUSTER_ID);

        setUriInfo(setUpBasicUriExpectations());
        setUpGetClusterExpectations(1);
        setUpGetNetworksByDataCenterExpectations(1, null);
        setUpActionExpectations(VdcActionType.AttachNetworkToCluster,
                AttachNetworkToClusterParameter.class,
                new String[] { "ClusterId" },
                new Object[] { CLUSTER_ID },
                valid,
                success);
        Network model = getModel(0);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddNameSuppliedButNoId() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        Network model = new Network();
        model.setName("orcus");
        model.setDescription(DESCRIPTIONS[0]);
        setUpEntityQueryExpectations(1, null);
        setUpGetClusterExpectations(1);
        setUpGetNetworksByDataCenterExpectations(1, null);
        setUpClusterExpectations(CLUSTER_ID);
        setUpActionExpectations(VdcActionType.AttachNetworkToCluster,
                AttachNetworkToClusterParameter.class,
                new String[] { "ClusterId" },
                new Object[] { CLUSTER_ID },
                true,
                true);
        collection.add(model);
    }

    @Test
    public void testAddIdSuppliedButNoName() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        Network model = new Network();
        model.setId("11111111-1111-1111-1111-111111111111");
        setUpEntityQueryExpectations(1, null);
        setUpGetClusterExpectations(1);
        setUpGetNetworksByDataCenterExpectations(1, null);
        setUpClusterExpectations(CLUSTER_ID);
        setUpActionExpectations(VdcActionType.AttachNetworkToCluster,
                AttachNetworkToClusterParameter.class,
                new String[] { "ClusterId" },
                new Object[] { CLUSTER_ID },
                true,
                true);
        collection.add(model);
    }

    @Test
    public void testAddIncompleteParametersNoName() throws Exception {
        Network model = new Network();
        model.setDescription(DESCRIPTIONS[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Network", "add", "id|name");
        }
    }

    @Override
    protected void setUpEntityQueryExpectations(int times, Object failure) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetAllNetworksByClusterId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { CLUSTER_ID },
                                         getEntityList(),
                                         failure);
        }
    }

    protected Cluster setUpClusterExpectations(Guid id) {
        Cluster group = control.createMock(Cluster.class);
        expect(group.getId()).andReturn(id).anyTimes();

        setUpEntityQueryExpectations(VdcQueryType.GetClusterByClusterId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { id },
                                     group);
        return group;
    }

    protected void setUpGetNetworksByDataCenterExpectations(int times, Object failure) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetAllNetworks,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { GUIDS[2] },
                                         getEntityList(),
                                         failure);
        }
    }

    protected void setUpGetClusterExpectations(int times) {
        while (times-- > 0) {
            Cluster cluster = new Cluster();
            cluster.setStoragePoolId(GUIDS[2]);
            setUpEntityQueryExpectations(VdcQueryType.GetClusterById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { CLUSTER_ID },
                    cluster,
                    null);
        }
    }

    @Override
    protected void setUpQueryExpectations(String query) throws Exception {
        setUpEntityQueryExpectations(1);
        control.replay();
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(1, failure);
        control.replay();
    }
}
