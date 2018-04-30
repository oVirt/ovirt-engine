package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendIscsiBondNetworksResourceTest extends AbstractBackendNetworksResourceTest<BackendNetworksResource> {
    protected static final Guid ISCSI_BOND_ID = GUIDS[1];
    protected static final Guid NETWORK_ID = GUIDS[2];

    public BackendIscsiBondNetworksResourceTest() {
        super(new BackendIscsiBondNetworksResource(ISCSI_BOND_ID.toString()), SearchType.IscsiBond, "IscsiBonds : ");
    }

    private org.ovirt.engine.core.common.businessentities.IscsiBond getIscsiBondContainingNetwork() {
        org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond = getIscsiBondWithNoNetworks();
        iscsiBond.getNetworkIds().add(NETWORK_ID);
        return iscsiBond;
    }

    private org.ovirt.engine.core.common.businessentities.IscsiBond getIscsiBondWithNoNetworks() {
        org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond =
                new org.ovirt.engine.core.common.businessentities.IscsiBond();
        iscsiBond.setId(ISCSI_BOND_ID);
        return iscsiBond;
    }

    @Test
    public void testAdd() {
        setUriInfo(setUpBasicUriExpectations());
        Network network = getModel(0);
        network.setId(NETWORK_ID.toString());

        setUpGetEntityExpectations(QueryType.GetIscsiBondById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { ISCSI_BOND_ID },
                getIscsiBondWithNoNetworks());

        setUpActionExpectations(ActionType.EditIscsiBond,
                EditIscsiBondParameters.class,
                new String[] { "IscsiBond" },
                new Object[] { getIscsiBondContainingNetwork() },
                true,
                true,
                null);

        Response response = collection.add(network);
        assertEquals(200, response.getStatus());
    }

    @Override
    protected void setUpEntityQueryExpectations(int times, Object failure) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(QueryType.GetNetworksByIscsiBondId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { ISCSI_BOND_ID },
                    getEntityList(),
                    failure);
        }
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(QueryType.GetNetworksByIscsiBondId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { ISCSI_BOND_ID },
                getEntityList(),
                failure);
    }

}
