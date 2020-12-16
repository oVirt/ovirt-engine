package org.ovirt.engine.api.restapi.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendDataCenterClustersResourceTest extends
        AbstractBackendCollectionResourceTest<org.ovirt.engine.api.model.Cluster, Cluster, BackendDataCenterClustersResource> {

    static Guid dataCenterId = GUIDS[1];

    public BackendDataCenterClustersResourceTest() {
        super(new BackendDataCenterClustersResource(dataCenterId.toString()), null, "");
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
    public void testAddIncompleteParameters() {
        org.ovirt.engine.api.model.Cluster model = new org.ovirt.engine.api.model.Cluster();
        setUriInfo(setUpBasicUriExpectations());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> collection.add(model)), "Cluster", "add", "name");
    }

    @Override
    protected void setUpQueryExpectations(String query) {
        setUpQueryExpectations(query, null);
    }

    @Override
    protected void setUpQueryExpectations(String query, Object failure) {
        setUpEntityQueryExpectations(QueryType.GetClustersByStoragePoolId,
                                     IdQueryParameters.class,
                                     new String[] { "Id" },
                                     new Object[] { GUIDS[1] },
                                     setUpClusters(),
                                     failure);
    }

    protected List<Cluster> setUpClusters() {
        List<Cluster> entities = new ArrayList<>();
        for (int i = 0; i < NAMES.length; i++) {
            entities.add(getEntity(i));
        }
        return entities;
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
