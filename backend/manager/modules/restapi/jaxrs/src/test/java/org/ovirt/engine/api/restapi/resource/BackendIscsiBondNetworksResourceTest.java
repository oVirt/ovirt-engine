package org.ovirt.engine.api.restapi.resource;

import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

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
    public void testAdd() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        Network network = getModel(0);
        network.setId(NETWORK_ID.toString());

        setUpGetEntityExpectations(VdcQueryType.GetIscsiBondById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { ISCSI_BOND_ID },
                getIscsiBondWithNoNetworks());

        setUpActionExpectations(VdcActionType.EditIscsiBond,
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
    protected void setUpEntityQueryExpectations(int times, Object failure) throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetNetworksByIscsiBondId,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { ISCSI_BOND_ID },
                    getEntityList(),
                    failure);
        }
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetNetworksByIscsiBondId,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { ISCSI_BOND_ID },
                getEntityList(),
                failure);
        control.replay();
    }

}
