package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendDataCenterNetworksResourceTest
        extends AbstractBackendNetworksResourceTest<BackendNetworksResource> {

    private static final Guid DATA_CENTER_ID = GUIDS[1];

    public BackendDataCenterNetworksResourceTest() {
        super(new BackendDataCenterNetworksResource(DATA_CENTER_ID.toString()), null, "");
    }

    @Test
    public void testAddNetwork() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(VdcActionType.AddNetwork,
                                  AddNetworkStoragePoolParameters.class,
                                  new String[] { "StoragePoolId" },
                                  new Object[] { DATA_CENTER_ID },
                                  true,
                                  true,
                                  null, //GUIDS[0],
                                  VdcQueryType.GetNetworksByDataCenterId,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { DATA_CENTER_ID },
                                  asList(getEntity(0)));
        Network model = getModel(0);
        model.setDataCenter(new DataCenter());
        model.getDataCenter().setId(DATA_CENTER_ID.toString());

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
        setUriInfo(setUpActionExpectations(VdcActionType.AddNetwork,
                                           AddNetworkStoragePoolParameters.class,
                                           new String[] { "StoragePoolId" },
                                           new Object[] { DATA_CENTER_ID },
                                           valid,
                                           success));
        Network model = getModel(0);
        model.setDataCenter(new DataCenter());
        model.getDataCenter().setId(DATA_CENTER_ID.toString());

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Network model = new Network();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Network", "add", "name");
        }
    }

    @Override
    protected void setUpQueryExpectations(String query) throws Exception {
        setUpQueryExpectations(query, null);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetNetworksByDataCenterId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { DATA_CENTER_ID },
                                         getEntityList(),
                                         failure);
        control.replay();
    }

    @Override
    protected void setUpEntityQueryExpectations(int times, Object failure) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetNetworksByDataCenterId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { DATA_CENTER_ID },
                                         getEntityList(),
                                         failure);
        }
    }
}
