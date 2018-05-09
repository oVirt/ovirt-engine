package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.EditIscsiBondParameters;
import org.ovirt.engine.core.common.businessentities.IscsiBond;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendIscsiBondNetworkResourceTest
        extends AbstractBackendNetworkResourceTest<BackendIscsiBondNetworkResource> {

    protected static final Guid ISCSI_BOND_ID = GUIDS[1];
    protected static final Guid NETWORK_ID = GUIDS[2];

    public BackendIscsiBondNetworkResourceTest() {
        super(new BackendIscsiBondNetworkResource(GUIDS[2].toString(),
                new BackendIscsiBondNetworksResource(ISCSI_BOND_ID.toString())));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, getIscsiBondContainingNetwork());
        setUpEntityQueryExpectations(QueryType.GetNetworkById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { NETWORK_ID },
                getEntityList());

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testGetWithInvalidNetworkId() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, getIscsiBondWithNoMatchingNetworks());
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testGetNetworkNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, getIscsiBondContainingNetwork());

        List<org.ovirt.engine.core.common.businessentities.network.Network> entities = new ArrayList<>();
        setUpEntityQueryExpectations(QueryType.GetNetworkById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { NETWORK_ID },
                entities);

        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testRemove() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(2, getIscsiBondContainingNetwork());
        setUpEntityQueryExpectations(
            QueryType.GetNetworkById,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { NETWORK_ID },
            getEntityList()
        );
        setUpActionExpectations(
            ActionType.EditIscsiBond,
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

    protected void setUpEntityQueryExpectations(int times, IscsiBond iscsiBond) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(QueryType.GetIscsiBondById,
                    IdQueryParameters.class,
                    new String[]{"Id"},
                    new Object[]{ISCSI_BOND_ID},
                    iscsiBond);
        }
    }

    private org.ovirt.engine.core.common.businessentities.IscsiBond getIscsiBondContainingNetwork() {
        org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond = new org.ovirt.engine.core.common.businessentities.IscsiBond();
        iscsiBond.setId(ISCSI_BOND_ID);
        iscsiBond.getNetworkIds().add(NETWORK_ID);
        return iscsiBond;
    }

    private org.ovirt.engine.core.common.businessentities.IscsiBond getIscsiBondWithNoMatchingNetworks() {
        org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond = new org.ovirt.engine.core.common.businessentities.IscsiBond();
        iscsiBond.setId(ISCSI_BOND_ID);
        iscsiBond.getNetworkIds().add(GUIDS[0]);
        return iscsiBond;
    }

    private org.ovirt.engine.core.common.businessentities.IscsiBond getIscsiBondWithNoNetworks() {
        org.ovirt.engine.core.common.businessentities.IscsiBond iscsiBond =
            new org.ovirt.engine.core.common.businessentities.IscsiBond();
        iscsiBond.setId(ISCSI_BOND_ID);
        return iscsiBond;
    }
}
