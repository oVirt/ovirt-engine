package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendDataCenterNetworksResourceTest
        extends AbstractBackendNetworksResourceTest<BackendDataCenterNetworksResource> {

    private static final Guid DATA_CENTER_ID = GUIDS[1];

    public BackendDataCenterNetworksResourceTest() {
        super(new BackendDataCenterNetworksResource(DATA_CENTER_ID.toString()), null, "");
    }

    @Test
    public void testAddNetwork() {
        setUriInfo(setUpBasicUriExpectations());
        setUpCreationExpectations(ActionType.AddNetwork,
                                  AddNetworkStoragePoolParameters.class,
                                  new String[] { "StoragePoolId" },
                                  new Object[] { DATA_CENTER_ID },
                                  true,
                                  true,
                                  null, //GUIDS[0],
                                  QueryType.GetNetworksByDataCenterId,
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
    public void testAddNetworkCantDo() {
        doTestBadAddNetwork(false, true, CANT_DO);
    }

    @Test
    public void testAddNetworkFailure() {
        doTestBadAddNetwork(true, false, FAILURE);
    }

    private void doTestBadAddNetwork(boolean valid, boolean success, String detail) {
        setUriInfo(setUpActionExpectations(ActionType.AddNetwork,
                                           AddNetworkStoragePoolParameters.class,
                                           new String[] { "StoragePoolId" },
                                           new Object[] { DATA_CENTER_ID },
                                           valid,
                                           success));
        Network model = getModel(0);
        model.setDataCenter(new DataCenter());
        model.getDataCenter().setId(DATA_CENTER_ID.toString());

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddIncompleteParameters() {
        Network model = new Network();
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)), "Network", "add", "name");
    }

    @Override
    protected void setUpQueryExpectations(String query) {
        setUpQueryExpectations(query, null);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(QueryType.GetNetworksByDataCenterId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { DATA_CENTER_ID },
                                         getEntityList(),
                                         failure);
    }

    @Override
    protected void setUpEntityQueryExpectations(int times, Object failure) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(QueryType.GetNetworksByDataCenterId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { DATA_CENTER_ID },
                                         getEntityList(),
                                         failure);
        }
    }
}
