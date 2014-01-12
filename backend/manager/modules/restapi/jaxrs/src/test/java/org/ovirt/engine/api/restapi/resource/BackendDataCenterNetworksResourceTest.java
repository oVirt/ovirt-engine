package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.RemoveNetworkParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
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
    public void testRemoveNotFound() throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetNetworksByDataCenterId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { DATA_CENTER_ID },
                                     new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>());
        control.replay();
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpEntityQueryExpectations(2);
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveNetwork,
                                           RemoveNetworkParameters.class,
                                           new String[] { "Id" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpEntityQueryExpectations(VdcQueryType.GetNetworksByDataCenterId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { DATA_CENTER_ID },
                                     new LinkedList<org.ovirt.engine.core.common.businessentities.network.Network>(),
                                     null);
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
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

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUpEntityQueryExpectations(2);

        setUriInfo(setUpActionExpectations(VdcActionType.RemoveNetwork,
                                           RemoveNetworkParameters.class,
                                           new String[] { "Id" },
                                           new Object[] { GUIDS[0] },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
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

    private void doTestBadAddNetwork(boolean canDo, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.AddNetwork,
                                           AddNetworkStoragePoolParameters.class,
                                           new String[] { "StoragePoolId" },
                                           new Object[] { DATA_CENTER_ID },
                                           canDo,
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

    protected StoragePool setUpStoragePool(Guid id) {
        StoragePool pool = control.createMock(StoragePool.class);
        expect(pool.getId()).andReturn(id).anyTimes();
        return pool;
    }
}
