package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
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
    extends AbstractBackendCollectionResourceTest<Network, org.ovirt.engine.core.common.businessentities.network.Network, BackendDataCenterNetworksResource> {

    private static final Guid DATA_CENTER_ID = GUIDS[1];

    public BackendDataCenterNetworksResourceTest() {
        super(new BackendDataCenterNetworksResource(DATA_CENTER_ID), null, null);
    }

    @Test
    public void testAddNetwork() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(
            VdcActionType.AddNetwork,
            AddNetworkStoragePoolParameters.class,
            new String[] { "StoragePoolId" },
            new Object[] { DATA_CENTER_ID },
            true,
            true,
            GUIDS[0],
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { GUIDS[0] },
            getEntity(0)
        );

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
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.AddNetwork,
                    AddNetworkStoragePoolParameters.class,
                    new String[] { "StoragePoolId" },
                    new Object[] { DATA_CENTER_ID },
                    valid,
                    success
            )
        );

        Network model = getModel(0);
        model.setDataCenter(new DataCenter());
        model.getDataCenter().setId(DATA_CENTER_ID.toString());

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        Network model = new Network();
        setUriInfo(setUpBasicUriExpectations());
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        }
        catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Network", "add", "name");
        }
    }

    @Override
    protected void setUpQueryExpectations(String query) throws Exception {
        setUpQueryExpectations(query, null);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworksByDataCenterId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { DATA_CENTER_ID },
            getEntityList(),
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
        when(entity.getDataCenterId()).thenReturn(GUIDS[1]);
        return entity;
    }

    @Override
    protected List<Network> getCollection() {
        return collection.list().getNetworks();
    }

    private static Network getModel(int index) {
        Network model = new Network();
        model.setId(GUIDS[0].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    private List<org.ovirt.engine.core.common.businessentities.network.Network> getEntityList() {
        List<org.ovirt.engine.core.common.businessentities.network.Network> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }
}
