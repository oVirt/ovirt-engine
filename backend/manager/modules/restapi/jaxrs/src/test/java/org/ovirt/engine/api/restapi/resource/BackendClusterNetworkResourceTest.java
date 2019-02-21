package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.BackendClusterNetworksResourceTest.CLUSTER_ID;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.model.NetworkUsage;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendClusterNetworkResourceTest
    extends AbstractBackendNetworkResourceTest<BackendClusterNetworkResource> {

    public BackendClusterNetworkResourceTest() {
        super(new BackendClusterNetworkResource(CLUSTER_ID.toString(),
              new BackendClusterNetworksResource(CLUSTER_ID.toString())));
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(
                assertThrows(WebApplicationException.class, () -> new BackendClusterNetworkResource("foo", null)));
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetAllNetworksByClusterId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { CLUSTER_ID },
                                     new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>());

        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.get()));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, false, false, false, false);

        verifyModel(resource.get(), 1);
    }

    @Test
    public void testUpdate() {
        setUpEntityQueryExpectations(1, false, false, false, false);
        setUpEntityQueryExpectations(1, true, true, true, true);
        setUriInfo(setUpActionExpectations(ActionType.UpdateNetworkOnCluster,
                                           NetworkClusterParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        verifyUpdate(resource.update(getModel(0)));
    }

    @Test
    public void testRemoveNotFound() {
        setUpEntityQueryExpectations(
            QueryType.GetAllNetworksByClusterId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { CLUSTER_ID },
            new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>()
        );

        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemove() {
        setUpEntityQueryExpectations(2, false, false, false, false);
        setUriInfo(
            setUpActionExpectations(
                ActionType.DetachNetworkToCluster,
                AttachNetworkToClusterParameter.class,
                new String[] { "ClusterId" },
                new Object[] { CLUSTER_ID },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveCantDo() {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUpEntityQueryExpectations(2, false, false, false, false);
        setUriInfo(
            setUpActionExpectations(
                ActionType.DetachNetworkToCluster,
                AttachNetworkToClusterParameter.class,
                new String[] { "ClusterId" },
                new Object[] { CLUSTER_ID },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    protected Cluster setUpClusterExpectations(Guid id) {
        Cluster group = mock(Cluster.class);
        when(group.getId()).thenReturn(id);

        setUpEntityQueryExpectations(QueryType.GetClusterById,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { id },
                                     group);
        return group;
    }
    private Network getModel(int i) {
        Network network = new Network();
        network.setId(GUIDS[i].toString());
        network.setName(NAMES[i]);
        network.setDisplay(true);
        return network;
    }
    protected void verifyUpdate(Network model) {
        assertTrue(model.isSetDisplay());
        assertEquals(true, model.isDisplay());
        assertTrue(model.isSetUsages());
        assertNotNull(model.getUsages().getUsages());
        assertTrue(model.getUsages().getUsages().contains(NetworkUsage.DISPLAY));
        assertTrue(model.getUsages().getUsages().contains(NetworkUsage.MIGRATION));
        assertTrue(model.getUsages().getUsages().contains(NetworkUsage.DEFAULT_ROUTE));
        assertTrue(model.isSetRequired());
        assertEquals(true, model.isRequired());
   }


    protected void setUpEntityQueryExpectations(int times, boolean isDisplay, boolean isMigration, boolean isRequired, boolean isDefaultRoute) {
        while (times-- > 0) {
            setUpEntityQueryExpectations(QueryType.GetAllNetworksByClusterId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { CLUSTER_ID },
                                         getEntityList(isDisplay, isMigration, isRequired, isDefaultRoute));
        }
    }
}

