package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.expect;

import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.Cpu;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.core.common.action.ManagementNetworkOnClusterOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.MigrationBandwidthLimitType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendClustersResourceTest extends
        AbstractBackendCollectionResourceTest<org.ovirt.engine.api.model.Cluster, Cluster, BackendClustersResource> {

    public BackendClustersResourceTest() {
        super(new BackendClustersResource(), SearchType.Cluster, "Clusters : ");
    }


    @Test
    public void testAddClusterFallbackVersion() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(VdcQueryType.GetStoragePoolById,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpStoragePool(-1));

        setUpGetEntityExpectations(VdcQueryType.GetManagementNetwork,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[0] },
                                   null);

        setUpCreationExpectations(VdcActionType.AddCluster,
                                  ManagementNetworkOnClusterOperationParameters.class,
                                  new String[] { "Cluster.CompatibilityVersion" },
                                  new Object[] { new org.ovirt.engine.core.compat.Version(2, 2) },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetClusterById,
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
    public void testAddClusterSpecificVersion() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(VdcQueryType.GetStoragePoolById,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpStoragePool(-1));

        setUpGetEntityExpectations(VdcQueryType.GetManagementNetwork,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[0] },
                                   null);

        setUpCreationExpectations(VdcActionType.AddCluster,
                                  ManagementNetworkOnClusterOperationParameters.class,
                                  new String[] { "Cluster.CompatibilityVersion" },
                                  new Object[] { new org.ovirt.engine.core.compat.Version(2, 3) },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetClusterById,
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
    public void testAddClusterCantDo() throws Exception {
        doTestBadAddCluster(false, true, CANT_DO);
    }

    @Test
    public void testAddClusterFailure() throws Exception {
        doTestBadAddCluster(true, false, FAILURE);
    }

    private void doTestBadAddCluster(boolean valid, boolean success, String detail)
            throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStoragePoolById,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpStoragePool(-1));

        setUriInfo(setUpActionExpectations(VdcActionType.AddCluster,
                                           ManagementNetworkOnClusterOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           valid,
                                           success));
        org.ovirt.engine.api.model.Cluster model = getModel(0);
        model.getDataCenter().setId(GUIDS[1].toString());

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddClusterNamedDataCenter() throws Exception {
        setUriInfo(setUpBasicUriExpectations());

        setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolByDatacenterName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                setUpStoragePool(1));

        setUpGetEntityExpectations(VdcQueryType.GetManagementNetwork,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                null);

        setUpCreationExpectations(VdcActionType.AddCluster,
                                  ManagementNetworkOnClusterOperationParameters.class,
                                  new String[] {},
                                  new Object[] {},
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetClusterById,
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
    public void testAddClusterCantDoNamedDataCenter() throws Exception {
        doTestBadAddClusterNamedDataCenter(false, true, CANT_DO);
    }

    @Test
    public void testAddClusterFailureNamedDataCenter() throws Exception {
        doTestBadAddClusterNamedDataCenter(true, false, FAILURE);
    }

    private void doTestBadAddClusterNamedDataCenter(boolean valid, boolean success, String detail)
            throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolByDatacenterName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                setUpStoragePool(1));

        setUriInfo(setUpActionExpectations(VdcActionType.AddCluster,
                                           ManagementNetworkOnClusterOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           valid,
                                           success));
        org.ovirt.engine.api.model.Cluster model = getModel(0);
        model.getDataCenter().setName(NAMES[1]);

        try {
            collection.add(model);
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddIncompleteParameters() throws Exception {
        org.ovirt.engine.api.model.Cluster model = new org.ovirt.engine.api.model.Cluster();
        model.setName(NAMES[0]);
        setUriInfo(setUpBasicUriExpectations());
        control.replay();
        try {
            collection.add(model);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
             verifyIncompleteException(wae, "Cluster", "add", "dataCenter.name|id");
        }
    }

    protected StoragePool setUpStoragePool(int index) {
        StoragePool pool = control.createMock(StoragePool.class);
        org.ovirt.engine.core.compat.Version version =
            new org.ovirt.engine.core.compat.Version(2, 2);
        if (index != -1) {
            expect(pool.getId()).andReturn(GUIDS[index]).anyTimes();
        }
        expect(pool.getCompatibilityVersion()).andReturn(version).anyTimes();
        return pool;
    }

    @Override
    protected Cluster getEntity(int index) {
        return setUpEntityExpectations(control.createMock(Cluster.class), index);
    }

    static Cluster setUpEntityExpectations(Cluster entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        expect(entity.getMigrationBandwidthLimitType()).andReturn(MigrationBandwidthLimitType.AUTO).anyTimes();
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
