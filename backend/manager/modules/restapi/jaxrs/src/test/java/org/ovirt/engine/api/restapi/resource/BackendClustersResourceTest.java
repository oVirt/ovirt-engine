package org.ovirt.engine.api.restapi.resource;

import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.Test;
import org.ovirt.engine.api.model.CPU;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.DataCenter;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.core.common.action.AddClusterOperationParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdsGroupParametersBase;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.NameQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

import static org.easymock.EasyMock.expect;

public class BackendClustersResourceTest extends
        AbstractBackendCollectionResourceTest<Cluster, VDSGroup, BackendClustersResource> {

    public BackendClustersResourceTest() {
        super(new BackendClustersResource(), SearchType.Cluster, "Clusters : ");
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations();
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVdsGroup,
                                           VdsGroupParametersBase.class,
                                           new String[] { "VdsGroupId" },
                                           new Object[] { GUIDS[0] },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
    }

    @Test
    public void testRemoveNonExistant() throws Exception{
        setUpGetEntityExpectations(NON_EXISTANT_GUID, true);
        control.replay();
        try {
            collection.remove(NON_EXISTANT_GUID.toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertNotNull(wae.getResponse());
            assertEquals(404, wae.getResponse().getStatus());
        }
    }

    private void setUpGetEntityExpectations(Guid entityId, Boolean returnNull) throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetVdsGroupById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { entityId },
                returnNull ? null : getEntity(0));
    }

    private void setUpGetEntityExpectations() throws Exception {
        setUpGetEntityExpectations(GUIDS[0], false);
    }

    @Test
    public void testRemoveCantDo() throws Exception {
        setUpGetEntityExpectations();
        doTestBadRemove(false, true, CANT_DO);
    }

    @Test
    public void testRemoveFailed() throws Exception {
        setUpGetEntityExpectations();
        doTestBadRemove(true, false, FAILURE);
    }

    protected void doTestBadRemove(boolean canDo, boolean success, String detail) throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.RemoveVdsGroup,
                                           VdsGroupParametersBase.class,
                                           new String[] { "VdsGroupId" },
                                           new Object[] { GUIDS[0] },
                                           canDo,
                                           success));
        try {
            collection.remove(GUIDS[0].toString());
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, detail);
        }
    }

    @Test
    public void testAddClusterFallbackVersion() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(VdcQueryType.GetStoragePoolById,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpStoragePool(-1));

        setUpCreationExpectations(VdcActionType.AddVdsGroup,
                                  AddClusterOperationParameters.class,
                                  new String[] { "VdsGroup.CompatibilityVersion" },
                                  new Object[] { new org.ovirt.engine.core.compat.Version(2, 2) },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetVdsGroupById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Cluster model = getModel(0);
        model.getDataCenter().setId(GUIDS[1].toString());

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Cluster);
        verifyModel((Cluster) response.getEntity(), 0);
    }

    @Test
    public void testAddClusterSpecificVersion() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(VdcQueryType.GetStoragePoolById,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpStoragePool(-1));

        setUpCreationExpectations(VdcActionType.AddVdsGroup,
                                  AddClusterOperationParameters.class,
                                  new String[] { "VdsGroup.CompatibilityVersion" },
                                  new Object[] { new org.ovirt.engine.core.compat.Version(2, 3) },
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetVdsGroupById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Cluster model = getModel(0);
        model.getDataCenter().setId(GUIDS[1].toString());
        model.setVersion(new Version());
        model.getVersion().setMajor(2);
        model.getVersion().setMinor(3);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Cluster);
        verifyModel((Cluster) response.getEntity(), 0);
    }

    @Test
    public void testAddClusterCantDo() throws Exception {
        doTestBadAddCluster(false, true, CANT_DO);
    }

    @Test
    public void testAddClusterFailure() throws Exception {
        doTestBadAddCluster(true, false, FAILURE);
    }

    private void doTestBadAddCluster(boolean canDo, boolean success, String detail)
            throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetStoragePoolById,
                                   IdQueryParameters.class,
                                   new String[] { "Id" },
                                   new Object[] { GUIDS[1] },
                                   setUpStoragePool(-1));

        setUriInfo(setUpActionExpectations(VdcActionType.AddVdsGroup,
                                           AddClusterOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           canDo,
                                           success));
        Cluster model = getModel(0);
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

        setUpCreationExpectations(VdcActionType.AddVdsGroup,
                                  AddClusterOperationParameters.class,
                                  new String[] {},
                                  new Object[] {},
                                  true,
                                  true,
                                  GUIDS[0],
                                  VdcQueryType.GetVdsGroupById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Cluster model = getModel(0);
        model.getDataCenter().setName(NAMES[1]);

        Response response = collection.add(model);
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof Cluster);
        verifyModel((Cluster) response.getEntity(), 0);
    }

    @Test
    public void testAddClusterCantDoNamedDataCenter() throws Exception {
        doTestBadAddClusterNamedDataCenter(false, true, CANT_DO);
    }

    @Test
    public void testAddClusterFailureNamedDataCenter() throws Exception {
        doTestBadAddClusterNamedDataCenter(true, false, FAILURE);
    }

    private void doTestBadAddClusterNamedDataCenter(boolean canDo, boolean success, String detail)
            throws Exception {
        setUpEntityQueryExpectations(VdcQueryType.GetStoragePoolByDatacenterName,
                NameQueryParameters.class,
                new String[] { "Name" },
                new Object[] { NAMES[1] },
                setUpStoragePool(1));

        setUriInfo(setUpActionExpectations(VdcActionType.AddVdsGroup,
                                           AddClusterOperationParameters.class,
                                           new String[] {},
                                           new Object[] {},
                                           canDo,
                                           success));
        Cluster model = getModel(0);
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
        Cluster model = new Cluster();
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
    protected VDSGroup getEntity(int index) {
        return setUpEntityExpectations(control.createMock(VDSGroup.class), index);
    }

    static VDSGroup setUpEntityExpectations(VDSGroup entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getDescription()).andReturn(DESCRIPTIONS[index]).anyTimes();
        return entity;
    }

    static Cluster getModel(int index) {
        Cluster model = new Cluster();
        model.setName(NAMES[index]);
        model.setDescription(DESCRIPTIONS[index]);
        model.setDataCenter(new DataCenter());
        model.setCpu(new CPU());
        model.getCpu().setId("Intel Xeon");
        return model;
    }

    @Override
    protected List<Cluster> getCollection() {
        return collection.list().getClusters();
    }

}
