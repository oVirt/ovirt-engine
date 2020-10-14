package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.BackendClustersResourceTest.getModel;
import static org.ovirt.engine.api.restapi.resource.BackendClustersResourceTest.setUpEntityExpectations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.action.ClusterParametersBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendDataCenterClusterResourceTest
        extends AbstractBackendSubResourceTest<org.ovirt.engine.api.model.Cluster, Cluster, BackendClusterResource<BackendDataCenterClustersResource>> {

    private static final Guid MANAGEMENT_NETWORK_ID = Guid.newGuid();
    private static final Guid clusterId = GUIDS[0];
    private static final Guid dataCenterId = GUIDS[1];

    private boolean isPopulateSet = false;

    public BackendDataCenterClusterResourceTest() {
        super(new BackendDataCenterClusterResource(
                new BackendDataCenterClustersResource(dataCenterId.toString()),
                clusterId.toString()));
    }

    @BeforeEach
    public void initParent() {
        initResource(resource.parent);
    }

    @Override
    protected void setUriInfo(UriInfo uriInfo) {
        resource.setUriInfo(uriInfo);
        ((BackendDataCenterClusterResource)resource).getParent().setUriInfo(uriInfo);
    }

    @Override
    protected void init() {
        initResource(resource);
        initResource(((BackendDataCenterClusterResource)resource).getParent());
    }

    @Test
    public void testBadGuid() {
        verifyNotFoundException(assertThrows(
                WebApplicationException.class,
                () -> new BackendDataCenterClusterResource(
                        new BackendDataCenterClustersResource(dataCenterId.toString()),
                        "foo"))
        );
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetClustersByStoragePoolId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { dataCenterId },
                                     new ArrayList<Cluster>(),
                                     null);

        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.get()));
    }

    @Test
    public void testGet() {
        setUriInfo(setUpBasicUriExpectations());
        setUpEntityQueryExpectations(QueryType.GetClustersByStoragePoolId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { dataCenterId },
                                     setUpClusters(),
                                     null);

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testUpdateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))));
    }

    @Test
    public void testUpdate() {
        setUpGetEntityExpectations(2);
        setUpManagementNetworkExpectation();

        setUriInfo(setUpActionExpectations(ActionType.UpdateCluster,
                                           ClusterOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           true,
                                           true));

        final org.ovirt.engine.api.model.Cluster updatedCluster = resource.update(getModel(0));

        verifyModel(updatedCluster, 0);

        verifyManagementNetwork(updatedCluster);
    }

    private void verifyManagementNetwork(org.ovirt.engine.api.model.Cluster updatedCluster) {
        assertEquals(String.format("%s/%s/%s/%s/%s",
                        BASE_PATH,
                        "clusters",
                        GUIDS[0],
                        "networks",
                        MANAGEMENT_NETWORK_ID),
                updatedCluster.getManagementNetwork().getHref());
    }

    private void setUpManagementNetworkExpectation() {
        setUpPopulateExpectation();
        final Network mockNetwork = mock(Network.class);
        when(mockNetwork.getId()).thenReturn(MANAGEMENT_NETWORK_ID);

        setUpGetEntityExpectations(QueryType.GetManagementNetwork,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                mockNetwork);
    }

    private void setUpPopulateExpectation() {
        if (!isPopulateSet) {
            when(httpHeaders.getRequestHeader(BackendResource.ALL_CONTENT_HEADER)).thenReturn(Collections.singletonList("true"));
            isPopulateSet = true;
        }
    }

    @Test
    public void testUpdateCantDo() {
        doTestBadUpdate(false, true, CANT_DO);
    }

    @Test
    public void testUpdateFailed() {
        doTestBadUpdate(true, false, FAILURE);
    }

    private void doTestBadUpdate(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations(1);

        setUriInfo(setUpActionExpectations(ActionType.UpdateCluster,
                                           ClusterOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           valid,
                                           success));

        verifyFault(assertThrows(WebApplicationException.class, () -> resource.update(getModel(0))), detail);
    }

    @Test
    public void testConflictedUpdate() {
        setUpGetEntityExpectations(1);

        org.ovirt.engine.api.model.Cluster model = getModel(1);
        model.setId(GUIDS[1].toString());
        verifyImmutabilityConstraint(assertThrows(WebApplicationException.class, () -> resource.update(model)));
    }

    @Test
    public void testRemove() {
        setUpEntityQueryExpectations(
            QueryType.GetClustersByStoragePoolId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { dataCenterId },
            setUpClusters(),
            null
        );
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveCluster,
                ClusterParametersBase.class,
                new String[] { "ClusterId" },
                new Object[] { GUIDS[0] },
                true,
                true
            )
        );
        verifyRemove(resource.remove());
    }

    @Test
    public void testRemoveNonExistant() {
        setUpEntityQueryExpectations(
            QueryType.GetClustersByStoragePoolId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { dataCenterId },
            new ArrayList<Cluster>(),
            null
        );
        verifyNotFoundException(assertThrows(WebApplicationException.class, () -> resource.remove()));
    }

    @Test
    public void testRemoveCantDo() {
        setUpEntityQueryExpectations(
            QueryType.GetClustersByStoragePoolId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { dataCenterId },
            setUpClusters(),
            null
        );
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() {
        setUpEntityQueryExpectations(
            QueryType.GetClustersByStoragePoolId,
            IdQueryParameters.class,
            new String[] { "Id" },
            new Object[] { dataCenterId },
            setUpClusters(),
            null
        );
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean valid, boolean success, String detail) {
        setUriInfo(
            setUpActionExpectations(
                ActionType.RemoveCluster,
                ClusterParametersBase.class,
                new String[] { "ClusterId" },
                new Object[] { GUIDS[0] },
                valid,
                success
            )
        );

        verifyFault(assertThrows(WebApplicationException.class, resource::remove), detail);
    }

    protected void setUpGetEntityExpectations(int times) {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetClusterById,
                                       IdQueryParameters.class,
                                       new String[] { "Id" },
                                       new Object[] { GUIDS[0] },
                                       notFound ? null : getEntity(0));
        }
    }

    @Override
    protected Cluster getEntity(int index) {
        return setUpEntityExpectations(mock(Cluster.class), index);
    }

    protected List<Cluster> setUpClusters() {
        List<Cluster> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
    }
}
