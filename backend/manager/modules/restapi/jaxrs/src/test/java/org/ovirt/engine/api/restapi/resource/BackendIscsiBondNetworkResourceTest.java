package org.ovirt.engine.api.restapi.resource;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendIscsiBondNetworkResourceTest
    extends AbstractBackendSubResourceTest<Network, org.ovirt.engine.core.common.businessentities.network.Network, BackendIscsiBondNetworkResource> {

    private static final Guid NETWORK_ID = GUIDS[0];
    private static final Guid ISCSI_BOND_ID = GUIDS[1];

    public BackendIscsiBondNetworkResourceTest() {
        super(new BackendIscsiBondNetworkResource(ISCSI_BOND_ID, NETWORK_ID.toString()));
    }

    @Test
    public void testGet() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetIscsiBondById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { ISCSI_BOND_ID },
            getIscsiBondContainingNetwork()
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            getEntity(0)
        );

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testGetWithInvalidNetworkId() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetIscsiBondById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { ISCSI_BOND_ID },
            getIscsiBondWithNoMatchingNetworks()
        );

        try {
            resource.get();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNetworkNotFound() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetIscsiBondById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { ISCSI_BOND_ID },
            getIscsiBondContainingNetwork()
        );
        setUpEntityQueryExpectations(
            VdcQueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            null,
            null
        );

        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetIscsiBondById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { ISCSI_BOND_ID },
            getIscsiBondContainingNetwork()
        );
        setUpActionExpectations(
            VdcActionType.EditIscsiBond,
            EditIscsiBondParameters.class,
            new String[] { "IscsiBond" },
            new Object[] { getIscsiBondWithNoNetworks() },
            true,
            true,
            null
        );

        Response response = resource.remove();
        assertNotNull(response);
        assertEquals(200, response.getStatus());
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

    private org.ovirt.engine.core.common.businessentities.IscsiBond getIscsiBondContainingNetwork() {
        org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond =
            new org.ovirt.engine.core.common.businessentities.IscsiBond();
        iscsiBond.setId(ISCSI_BOND_ID);
        iscsiBond.getNetworkIds().add(NETWORK_ID);
        return iscsiBond;
    }

    private org.ovirt.engine.core.common.businessentities.IscsiBond getIscsiBondWithNoMatchingNetworks() {
        org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond =
            new org.ovirt.engine.core.common.businessentities.IscsiBond();
        iscsiBond.setId(ISCSI_BOND_ID);
        iscsiBond.getNetworkIds().add(GUIDS[1]);
        return iscsiBond;
    }

    private org.ovirt.engine.core.common.businessentities.IscsiBond getIscsiBondWithNoNetworks() {
        org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond =
            new org.ovirt.engine.core.common.businessentities.IscsiBond();
        iscsiBond.setId(ISCSI_BOND_ID);
        return iscsiBond;
    }
}
