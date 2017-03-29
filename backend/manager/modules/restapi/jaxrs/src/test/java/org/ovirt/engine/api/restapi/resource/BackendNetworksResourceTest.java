package org.ovirt.engine.api.restapi.resource;

import static java.util.Comparator.comparing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendNetworksResourceTest
    extends AbstractBackendCollectionResourceTest<Network, org.ovirt.engine.core.common.businessentities.network.Network, BackendNetworksResource> {

    private static final Guid DATA_CENTER_ID = GUIDS[1];

    public BackendNetworksResourceTest() {
        super(new BackendNetworksResource(), SearchType.Network, "Networks : ");
    }

    @Test
    public void testAddNetwork() throws Exception {
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
    public void testAddNetworkWithNamedDataCenter() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetStoragePoolByDatacenterName,
            NameQueryParameters.class,
            new String[] { "Name" },
            new Object[] { NAMES[1] },
            getStoragePool(DATA_CENTER_ID)
        );
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
        model.getDataCenter().setName(NAMES[1]);
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
        setUpActionExpectations(
            VdcActionType.AddNetwork,
            AddNetworkStoragePoolParameters.class,
            new String[] { "StoragePoolId" },
            new Object[] { DATA_CENTER_ID },
            valid,
            success
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
        model.setName(NAMES[0]);
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        }
        catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Network", "add", "dataCenter.name|id");
        }
    }

    @Test
    public void testQueryWithFilter() throws Exception {
        List<String> filterValue = Collections.singletonList("true");
        reset(httpHeaders);
        when(httpHeaders.getRequestHeader(USER_FILTER_HEADER)).thenReturn(filterValue);
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllNetworks,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { Guid.Empty },
            getNetworks()
        );
        List<Network> networks = getCollection();
        networks.sort(comparing(Network::getId));
        verifyCollection(networks);
    }

    @Override
    protected org.ovirt.engine.core.common.businessentities.network.Network getEntity(int index) {
        org.ovirt.engine.core.common.businessentities.network.Network entity = mock(
            org.ovirt.engine.core.common.businessentities.network.Network.class
        );
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getDataCenterId()).thenReturn(DATA_CENTER_ID);
        return entity;
    }

    @Override
    protected List<Network> getCollection() {
        return collection.list().getNetworks();
    }

    private Network getModel(int index) {
        Network model = new Network();
        model.setId(GUIDS[0].toString());
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        return model;
    }

    private StoragePool getStoragePool(Guid id) {
        StoragePool pool = mock(StoragePool.class);
        when(pool.getId()).thenReturn(id);
        return pool;
    }

    private List<org.ovirt.engine.core.common.businessentities.network.Network> getNetworks() {
        List<org.ovirt.engine.core.common.businessentities.network.Network> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }
}
