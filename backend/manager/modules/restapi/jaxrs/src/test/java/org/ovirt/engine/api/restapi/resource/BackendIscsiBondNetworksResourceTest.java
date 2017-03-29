package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendIscsiBondNetworksResourceTest
    extends AbstractBackendCollectionResourceTest<Network, org.ovirt.engine.core.common.businessentities.network.Network, BackendIscsiBondNetworksResource> {

    private static final Guid ISCSI_BOND_ID = GUIDS[1];
    private static final Guid NETWORK_ID = GUIDS[2];

    public BackendIscsiBondNetworksResourceTest() {
        super(new BackendIscsiBondNetworksResource(ISCSI_BOND_ID), null, null);
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
        setUpGetEntityExpectations(
            VdcQueryType.GetIscsiBondById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { ISCSI_BOND_ID },
            getIscsiBondWithNoNetworks()
        );
        setUpActionExpectations(
            VdcActionType.EditIscsiBond,
            EditIscsiBondParameters.class,
            new String[] { "IscsiBond" },
            new Object[] { getIscsiBondContainingNetwork() },
            true,
            true,
            null
        );
        setUpGetEntityExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            getEntity(0)
        );

        Network network = getModel(0);
        network.setId(NETWORK_ID.toString());
        Response response = collection.add(network);
        assertEquals(201, response.getStatus());
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

    @Override
    protected void setUpQueryExpectations(String query, Object failure) throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworksByIscsiBondId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { ISCSI_BOND_ID },
            getEntityList(),
            failure
        );
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
