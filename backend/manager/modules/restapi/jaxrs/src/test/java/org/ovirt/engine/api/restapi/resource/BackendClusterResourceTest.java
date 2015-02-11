package org.ovirt.engine.api.restapi.resource;

import java.util.Collections;

import javax.ws.rs.WebApplicationException;

import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.resource.BackendClustersResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendClustersResourceTest.setUpEntityExpectations;

public class BackendClusterResourceTest
        extends AbstractBackendSubResourceTest<Cluster, VDSGroup, BackendClusterResource<BackendClustersResource>> {

    private static final Guid MANAGEMENT_NETWORK_ID = Guid.newGuid();

    private boolean isPopulateSet = false;

    public BackendClusterResourceTest() {
        super(new BackendClusterResource(GUIDS[0].toString(), new BackendClustersResource()));
    }

    @Before
    public void initParent() {
        initResource(resource.parent);
    }

    @Test
    public void testBadGuid() throws Exception {
        control.replay();
        try {
            new BackendClusterResource("foo", new BackendClustersResource());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testGetNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
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
        setUpGetEntityExpectations(1);
        setUpManagementNetworkExpectation();
        control.replay();

        final Cluster cluster = resource.get();
        verifyModel(cluster, 0);

        verifyManagementNetwork(cluster);
    }

    @Test
    public void testUpdateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        control.replay();
        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyNotFoundException(wae);
        }
    }

    @Test
    public void testUpdate() throws Exception {
        setUpGetEntityExpectations(2);
        setUpManagementNetworkExpectation();

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVdsGroup,
                                           ManagementNetworkOnClusterOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        final Cluster updatedCluster = resource.update(getModel(0));

        verifyModel(updatedCluster, 0);

        verifyManagementNetwork(updatedCluster);
    }

    private void verifyManagementNetwork(Cluster updatedCluster) {
        assertEquals(String.format("%s/%s/%s/%s/%s",
                        BASE_PATH,
                        "clusters",
                        GUIDS[0],
                        "networks",
                        MANAGEMENT_NETWORK_ID),
                updatedCluster.getManagementNetwork().getHref());
    }

    @Test
    public void testUpdateCantDo() throws Exception {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() throws Exception {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean canDo, boolean success, String detail) throws Exception {
        setUpGetEntityExpectations(1);

        setUriInfo(setUpActionExpectations(VdcActionType.UpdateVdsGroup,
                                           ManagementNetworkOnClusterOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           canDo,
                                           success));

        try {
            resource.update(getModel(0));
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testConflictedUpdate() throws Exception {
        setUpGetEntityExpectations(1);
        control.replay();

        Cluster model = getModel(1);
        model.setId(GUIDS[1].toString());
        try {
            resource.update(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyImmutabilityConstraint(wae);
        }
    }

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetVdsGroupById,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : getEntity(0));
        }
    }

    private void setUpManagementNetworkExpectation() throws Exception {
        setUpPopulateExpectation();
        final Network mockNetwork = control.createMock(Network.class);
        expect(mockNetwork.getId()).andReturn(MANAGEMENT_NETWORK_ID);

        setUpGetEntityExpectations(VdcQueryType.GetManagementNetwork,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                mockNetwork);
    }

    private void setUpPopulateExpectation() {
        if (!isPopulateSet) {
            expect(httpHeaders.getRequestHeader(BackendResource.POPULATE)).andReturn(Collections.singletonList("true"))
                    .anyTimes();
            isPopulateSet = true;
        }
    }

    @Override
    protected VDSGroup getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VDSGroup.class), index);
    }
}
