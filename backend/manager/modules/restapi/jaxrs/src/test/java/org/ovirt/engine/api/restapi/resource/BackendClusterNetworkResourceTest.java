package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.BackendClusterNetworksResourceTest.CLUSTER_ID;

import java.util.ArrayList;

import javax.ws.rs.WebApplicationException;

import org.junit.Test;
import org.ovirt.engine.api.model.Network;
import org.ovirt.engine.api.restapi.types.NetworkUsage;
import org.ovirt.engine.core.common.action.AttachNetworkToClusterParameter;
import org.ovirt.engine.core.common.action.NetworkClusterParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendClusterNetworkResourceTest
    extends AbstractBackendNetworkResourceTest<BackendClusterNetworkResource> {

    public BackendClusterNetworkResourceTest() {
        super(new BackendClusterNetworkResource(CLUSTER_ID.toString(),
              new BackendClusterNetworksResource(CLUSTER_ID.toString())));
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendClusterNetworkResource("foo", null);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(VdcQueryType.GetAllNetworksByClusterId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { CLUSTER_ID },
                                     new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>());
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGet() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(1, false, false, false);
        control.replay();

        verifyModel(resource.get(), 1);
    }

    @Test
    public void testUpdate() throws Exception {
        setUpEntityQueryExpectations(1, false, false, false);
        setUpEntityQueryExpectations(1, true, true, true);
        setUpClusterExpectations(GUIDS[1]);
        setUriInfo(setUpActionExpectations(VdcActionType.UpdateNetworkOnCluster,
                                           NetworkClusterParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        verifyUpdate(resource.update(getModel(0)));
    }

    @Test
    public void testRemoveNotFound() throws Exception {
        setUpEntityQueryExpectations(
            VdcQueryType.GetAllNetworksByClusterId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { CLUSTER_ID },
            new ArrayList<org.ovirt.engine.core.common.businessentities.network.Network>()
        );
        control.replay();
        try {
            resource.remove();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testRemove() throws Exception {
        setUpClusterExpectations(CLUSTER_ID);
        setUpEntityQueryExpectations(2, false, false, false);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.DetachNetworkToCluster,
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
    public void testRemoveCantDo() throws Exception {
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) throws Exception {
        setUpClusterExpectations(CLUSTER_ID);
        setUpEntityQueryExpectations(2, false, false, false);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.DetachNetworkToCluster,
                AttachNetworkToClusterParameter.class,
                new String[] { "ClusterId" },
                new Object[] { CLUSTER_ID },
                valid,
                success
            )
        );
        try {
            resource.remove();
            fail("expected WebApplicationException");
        }
        catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    protected Cluster setUpClusterExpectations(Guid id) {
        Cluster group = control.createMock(Cluster.class);
        expect(group.getId()).andReturn(id).anyTimes();

        setUpEntityQueryExpectations(VdcQueryType.GetClusterByClusterId,
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
        assertTrue(model.getUsages().getUsages().contains(NetworkUsage.DISPLAY.value()));
        assertTrue(model.getUsages().getUsages().contains(NetworkUsage.MIGRATION.value()));
        assertTrue(model.isSetRequired());
        assertEquals(true, model.isRequired());
   }


    protected void setUpEntityQueryExpectations(int times, boolean isDisplay, boolean isMigration, boolean isRequired)
            throws Exception {
        while (times-- > 0) {
            setUpEntityQueryExpectations(VdcQueryType.GetAllNetworksByClusterId,
                                         IdQueryParameters.class,
                                         new String[] { "Id" },
                                         new Object[] { CLUSTER_ID },
                                         getEntityList(isDisplay, isMigration, isRequired));
        }
    }
}

