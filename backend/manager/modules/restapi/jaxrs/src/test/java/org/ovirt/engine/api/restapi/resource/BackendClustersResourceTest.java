package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Cpu;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ClusterOperationParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.FipsMode;
import org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendClustersResourceTest extends
        AbstractBackendCollectionResourceTest<org.ovirt.engine.api.model.Cluster, Cluster, BackendClustersResource> {

    public BackendClustersResourceTest() {
        super(new BackendClustersResource(), SearchType.Cluster, "Clusters : ");
    }


    @Test
    public void testAddClusterFallbackVersion() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(QueryType.GetStoragePoolById,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpStoragePool(-1));

        setUpGetEntityExpectations(QueryType.GetManagementNetwork,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[0] },
                                   null);

        setUpCreationExpectations(ActionType.AddCluster,
                                  ClusterOperationParameters.class,
                                  new String[] { "Cluster.CompatibilityVersion" },
                                  new Object[] { new org.ovirt.engine.core.compat.Version(2, 2) },
                                  true,
                                  true,
                                  GUIDS[0],
                                  QueryType.GetClusterById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        org.ovirt.engine.api.model.Cluster model = getModel(0);
        model.getDataCenter().setId(GUIDS[1].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof org.ovirt.engine.api.model.Cluster);
        verifyModel((org.ovirt.engine.api.model.Cluster) response.getEntity(), 0);
    }

    @Test
    public void testAddClusterSpecificVersion() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(QueryType.GetStoragePoolById,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpStoragePool(-1));

        setUpGetEntityExpectations(QueryType.GetManagementNetwork,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[0] },
                                   null);

        setUpCreationExpectations(ActionType.AddCluster,
                                  ClusterOperationParameters.class,
                                  new String[] { "Cluster.CompatibilityVersion" },
                                  new Object[] { new org.ovirt.engine.core.compat.Version(2, 3) },
                                  true,
                                  true,
                                  GUIDS[0],
                                  QueryType.GetClusterById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        org.ovirt.engine.api.model.Cluster model = getModel(0);
        model.getDataCenter().setId(GUIDS[1].toString());
        model.setVersion(new Version());
        model.getVersion().setMajor(2);
        model.getVersion().setMinor(3);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof org.ovirt.engine.api.model.Cluster);
        verifyModel((org.ovirt.engine.api.model.Cluster) response.getEntity(), 0);
    }

    @Test
    public void testAddClusterCantDo() {
        doTestBadAddCluster(false, true, CANT_DO);
    }

    @Test
    public void testAddClusterFailure() {
        doTestBadAddCluster(true, false, FAILURE);
    }

    private void doTestBadAddCluster(boolean valid, boolean success, String detail) {
        setUpGetEntityExpectations(QueryType.GetStoragePoolById,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpStoragePool(-1));

        setUriInfo(setUpActionExpectations(ActionType.AddCluster,
                                           ClusterOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           valid,
                                           success));
        org.ovirt.engine.api.model.Cluster model = getModel(0);
        model.getDataCenter().setId(GUIDS[1].toString());

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddClusterNamedDataCenter() {
        setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(QueryType.GetStoragePoolByDatacenterName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                setUpStoragePool(1));

        setUpGetEntityExpectations(QueryType.GetManagementNetwork,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                null);

        setUpCreationExpectations(ActionType.AddCluster,
                                  ClusterOperationParameters.class,
                                  new String[] {},
                                  new Object[] {},
                                  true,
                                  true,
                                  GUIDS[0],
                                  QueryType.GetClusterById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        org.ovirt.engine.api.model.Cluster model = getModel(0);
        model.getDataCenter().setName(NAMES[1]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof org.ovirt.engine.api.model.Cluster);
        verifyModel((org.ovirt.engine.api.model.Cluster) response.getEntity(), 0);
    }

    @Test
    public void testAddClusterCantDoNamedDataCenter() {
        doTestBadAddClusterNamedDataCenter(false, true, CANT_DO);
    }

    @Test
    public void testAddClusterFailureNamedDataCenter() {
        doTestBadAddClusterNamedDataCenter(true, false, FAILURE);
    }

    private void doTestBadAddClusterNamedDataCenter(boolean valid, boolean success, String detail) {
        setUpEntityQueryExpectations(QueryType.GetStoragePoolByDatacenterName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                setUpStoragePool(1));

        setUriInfo(setUpActionExpectations(ActionType.AddCluster,
                                           ClusterOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           valid,
                                           success));
        org.ovirt.engine.api.model.Cluster model = getModel(0);
        model.getDataCenter().setName(NAMES[1]);

        verifyFault(assertThrows(WebApplicationException.class, () -> collection.add(model)), detail);
    }

    @Test
    public void testAddIncompleteParameters() {
        org.ovirt.engine.api.model.Cluster model = new org.ovirt.engine.api.model.Cluster();
        model.setName(NAMES[0]);
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)),
                "Cluster", "add", "dataCenter.name|id");
    }

    protected StoragePool setUpStoragePool(int index) {
        StoragePool pool = mock(StoragePool.class);
        org.ovirt.engine.core.compat.Version version =
            new org.ovirt.engine.core.compat.Version(2, 2);
        if (index != -1) {
            when(pool.getId()).thenReturn(GUIDS[index]);
        }
        when(pool.getCompatibilityVersion()).thenReturn(version);
        return pool;
    }

    @Override
    protected Cluster getEntity(int index) {
        return setUpEntityExpectations(mock(Cluster.class), index);
    }

    static Cluster setUpEntityExpectations(Cluster entity, int index) {
        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getName()).thenReturn(NAMES[index]);
        when(entity.getDescription()).thenReturn(DESCRIPTIONS[index]);
        when(entity.getMigrationBandwidthLimitType()).thenReturn(MigrationBandwidthLimitType.AUTO);
        when(entity.getFipsMode()).thenReturn(FipsMode.UNDEFINED);
        return entity;
    }

    static org.ovirt.engine.api.model.Cluster getModel(int index) {
        org.ovirt.engine.api.model.Cluster model = new org.ovirt.engine.api.model.Cluster();
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        model.setDataCenter(new DataCenter());
        model.setCpu(new Cpu());
        model.getCpu().setType("Intel Xeon");
        return model;
    }

    @Override
    protected List<org.ovirt.engine.api.model.Cluster> getCollection() {
        return collection.list().getClusters();
    }

}
