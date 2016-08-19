package org.ovirt.engine.api.restapi.resource.gluster;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.gluster.GlusterTestHelper.brickDir;
import static org.ovirt.engine.api.restapi.resource.gluster.GlusterTestHelper.clusterId;
import static org.ovirt.engine.api.restapi.resource.gluster.GlusterTestHelper.serverId;
import static org.ovirt.engine.api.restapi.resource.gluster.GlusterTestHelper.volumeId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterBricks;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.api.restapi.resource.AbstractBackendResource;
import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeAdvancedDetailsParameters;
import org.ovirt.engine.core.compat.Guid;

public class BackendGlusterBricksResourceTest extends AbstractBackendCollectionResourceTest<GlusterBrick, GlusterBrickEntity, BackendGlusterBricksResource> {

    private static final String serverName = "testServer";
    private BackendGlusterVolumeResource parentMock;
    private GlusterTestHelper helper;
    private static final String MIGRATE_BRICKS_ACTION_BASE_URL = "/clusters/" + clusterId + "/glustervolumes/" + volumeId;

    public BackendGlusterBricksResourceTest() {
        super(new BackendGlusterBricksResource(),
                null,
                null);
    }

    /**
     * Override init to perform additional mocking required for the "list" method of the collection resource.
     */
    @Override
    protected void init() {
        super.init();
        setUpParentMocks();
        helper = new GlusterTestHelper();
    }

    @Override
    protected List<GlusterBrick> getCollection() {
        return collection.list().getGlusterBricks();
    }

    @Override
    protected GlusterBrickEntity getEntity(int index) {
        return helper.getBrickEntity(index, false);
    }

    @Override
    @Test
    public void testList() throws Exception {
        setUpBricksQueryExpectations(null);
        UriInfo uriInfo = setUpActionsUriExpectations();
        collection.setUriInfo(uriInfo);

        verifyCollection(getCollection());
    }

    @Test
    public void testListAllDetails() throws Exception {
        setUpBricksQueryExpectations(null);
        setUpGetEntityExpectationsAllContent(false);
        UriInfo uriInfo = setUpActionsUriExpectations();
        collection.setUriInfo(uriInfo);

        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testListFailure() throws Exception {
        setUpBricksQueryExpectations(FAILURE);
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertTrue(wae.getResponse().getEntity() instanceof Fault);
            assertEquals(mockl10n(FAILURE), ((Fault) wae.getResponse().getEntity()).getDetail());
        }
    }

    @Override
    @Test
    public void testListCrash() throws Exception {
        Throwable t = new RuntimeException(FAILURE);
        setUpBricksQueryExpectations(t);

        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);


        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, t);
        }
    }

    @Override
    @Test
    public void testListCrashClientLocale() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        locales.add(CLIENT_LOCALE);

        Throwable t = new RuntimeException(FAILURE);
        setUpBricksQueryExpectations(t);
        collection.setUriInfo(uriInfo);

        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_CLIENT_LOCALE, t);
        } finally {
            locales.clear();
        }
    }

    @Test
    public void testAdd() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        when(uriInfo.getPath()).thenReturn("clusters/" + clusterId + "/glustervolumes/" + volumeId + "/bricks");
        setUriInfo(uriInfo);
        setUpBrickCreationExpectation(false);
        Response response = collection.add(createModel());
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof GlusterBricks);
        verifyModel(((GlusterBricks) response.getEntity()).getGlusterBricks().get(0), 0);
    }

    @Test
    public void testAddForce() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        when(uriInfo.getPath()).thenReturn("clusters/" + clusterId + "/glustervolumes/" + volumeId + "/bricks");
        setUriInfo(setUpGetMatrixConstraintsExpectations(
                BackendResource.FORCE_CONSTRAINT,
                true,
                "true",
                uriInfo
        ));
        setUpBrickCreationExpectation(true);
        Response response = collection.add(createModel());
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof GlusterBricks);
        verifyModel(((GlusterBricks) response.getEntity()).getGlusterBricks().get(0), 0);
    }

    @Test
    public void testAddForceFalse() throws Exception {
        UriInfo uriInfo = setUpBasicUriExpectations();
        when(uriInfo.getPath()).thenReturn("clusters/" + clusterId + "/glustervolumes/" + volumeId + "/bricks");
        setUriInfo(setUpGetMatrixConstraintsExpectations(
                BackendResource.FORCE_CONSTRAINT,
                false,
                "false",
                uriInfo
        ));
        setUpBrickCreationExpectation(false);
        Response response = collection.add(createModel());
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof GlusterBricks);
        verifyModel(((GlusterBricks) response.getEntity()).getGlusterBricks().get(0), 0);
    }

    private void setUpBrickCreationExpectation(boolean force) {
        setUpCreationExpectations(VdcActionType.AddBricksToGlusterVolume,
                GlusterVolumeBricksActionParameters.class,
                new String[] { "VolumeId", "Bricks", "Force" },
                new Object[] { volumeId, getBricks(), force },
                true,
                true,
                getBrickIds(),
                VdcQueryType.GetGlusterBrickById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { GUIDS[0] },
                getEntity(0));
    }

    @Test
    public void testRemove() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetGlusterVolumeById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { volumeId },
                helper.getVolumeEntity(0));

        setUriInfo(setUpActionExpectations(VdcActionType.GlusterVolumeRemoveBricks,
                GlusterVolumeRemoveBricksParameters.class,
                new String[] { "VolumeId", "Bricks" },
                new Object[] { volumeId, getBrickEntitiesToRemove() },
                true,
                true));
        Action action = new Action();
        action.setBricks(getBrickModelsToRemove());
        verifyRemove(collection.remove(action));
    }

    @Test
    public void testRemoveCommit() throws Exception {
        setUpGetEntityExpectations(VdcQueryType.GetGlusterVolumeById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { volumeId },
                helper.getVolumeEntity(1));

        setUriInfo(setUpActionExpectations(VdcActionType.CommitRemoveGlusterVolumeBricks,
                GlusterVolumeRemoveBricksParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));
        Action action = new Action();
        action.setBricks(getBrickModelsToRemove());
        verifyRemove(collection.remove(action));
    }

    @Test
    public void testMigrate() throws Exception {
        GlusterBrick brick = new GlusterBrick();
        GlusterVolume volume = new GlusterVolume();
        brick.setName(serverName + ":" + brickDir);
        volume.setId(volumeId.toString());
        brick.setGlusterVolume(volume);

        GlusterBricks bricks = mock(GlusterBricks.class);
        when(bricks.getGlusterBricks()).thenReturn(Collections.singletonList(brick));

        setUriInfo(setUpActionExpectations(VdcActionType.StartRemoveGlusterVolumeBricks,
                GlusterVolumeRemoveBricksParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));

        Action action = new Action();
        action.setBricks(bricks);
        collection.migrate(action);
    }

    @Test
    public void testStopMigrate() throws Exception {
        GlusterBrick brick = new GlusterBrick();
        GlusterVolume volume = new GlusterVolume();
        brick.setName(serverName + ":" + brickDir);
        volume.setId(volumeId.toString());
        brick.setGlusterVolume(volume);

        GlusterBricks bricks = mock(GlusterBricks.class);
        when(bricks.getGlusterBricks()).thenReturn(Collections.singletonList(brick));

        setUriInfo(setUpActionExpectations(VdcActionType.StopRemoveGlusterVolumeBricks,
                GlusterVolumeRemoveBricksParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));

        Action action = new Action();
        action.setBricks(bricks);
        collection.stopMigrate(action);
    }

    @Test
    public void testActivate() throws Exception {
        GlusterBrick brick = new GlusterBrick();
        GlusterVolume volume = new GlusterVolume();
        brick.setName(serverName + ":" + brickDir);
        volume.setId(volumeId.toString());
        brick.setGlusterVolume(volume);

        GlusterBricks bricks = mock(GlusterBricks.class);
        when(bricks.getGlusterBricks()).thenReturn(Collections.singletonList(brick));

        setUriInfo(setUpActionExpectations(VdcActionType.StopRemoveGlusterVolumeBricks,
                GlusterVolumeRemoveBricksParameters.class,
                new String[] {},
                new Object[] {},
                true,
                true));

        Action action = new Action();
        action.setBricks(bricks);
        collection.stopMigrate(action);
    }

    /**
     * Overriding this as the bricks collection doesn't support search queries
     */
    @Override
    @Test
    public void testQuery() throws Exception {
        testList();
    }

    private List<Guid> getBrickIds() {
        List<Guid> brickIds = new ArrayList<>();
        brickIds.add(GUIDS[0]);
        return brickIds;
    }

    private List<GlusterBrickEntity> getBrickEntitiesToRemove() {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setId(GUIDS[0]);
        brick.setVolumeId(volumeId);
        bricks.add(brick);
        return bricks;
    }

    private GlusterBricks getBrickModelsToRemove() {
        GlusterBricks bricks = new GlusterBricks();
        GlusterBrick brick = new GlusterBrick();
        brick.setId(GUIDS[0].toString());
        bricks.getGlusterBricks().add(brick);
        return bricks;
    }

    private List<GlusterBrickEntity> getBricks() {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setId(GUIDS[0]);
        brick.setServerId(serverId);
        brick.setServerName(serverName);
        brick.setVolumeId(volumeId);
        brick.setBrickDirectory(brickDir);
        bricks.add(brick);
        return bricks;
    }

    /**
     * Overridden as {@link GlusterBrickEntity} does not have name/description field
     */
    @Override
    protected void verifyModel(GlusterBrick model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(volumeId.toString(), model.getGlusterVolume().getId());
        assertEquals(serverId.toString(), model.getServerId());
        assertEquals(brickDir, model.getBrickDir());
        verifyLinks(model);
    }

    private GlusterBricks createModel() {
        GlusterBricks bricks = new GlusterBricks();

        GlusterBrick brick = new GlusterBrick();
        brick.setId(GUIDS[0].toString());
        brick.setGlusterVolume(new GlusterVolume());
        brick.getGlusterVolume().setId(volumeId.toString());
        brick.setServerId(serverId.toString());
        brick.setBrickDir(brickDir);

        bricks.getGlusterBricks().add(brick);
        return bricks;
    }

    /**
     * The method {@link BackendGlusterBricksResource#list()} internally invokes
     * {@link BackendGlusterVolumeResource#get()} to fetch the volume object, and then invokes the query to fetch the
     * bricks of that volume. This method mocks the volume resource to return pre-defined volume id
     */
    private void setUpParentMocks() {
        GlusterVolume volume = new GlusterVolume();
        volume.setId(volumeId.toString());

        Cluster cluster = new Cluster();
        cluster.setId(clusterId.toString());
        volume.setCluster(cluster);

        ClusterResource clusterResourceMock = mock(ClusterResource.class);
        when(clusterResourceMock.get()).thenReturn(cluster);

        BackendGlusterVolumesResource volumesResourceMock = mock(BackendGlusterVolumesResource.class);
        when(volumesResourceMock.getParent()).thenReturn(clusterResourceMock);

        parentMock = mock(BackendGlusterVolumeResource.class);
        when(parentMock.getParent()).thenReturn(volumesResourceMock);
        when(parentMock.get()).thenReturn(volume);

        collection.setParent(parentMock);

        doAnswer(invocation -> {
            GlusterVolume model = (GlusterVolume) invocation.getArguments()[0];
            Cluster clusterModel = new Cluster();
            clusterModel.setId(clusterId.toString());
            model.setCluster(clusterModel);
            model.setId(volumeId.toString());
            return model;
        }).when(parentMock).addParents(isA(GlusterVolume.class));
    }

    private void setUpBricksQueryExpectations(Object failure) {
        VdcQueryReturnValue queryResult = mock(VdcQueryReturnValue.class);
        when(queryResult.getSucceeded()).thenReturn(failure == null);
        List<GlusterBrickEntity> entities = new ArrayList<>();

        if (failure == null) {
            for (int i = 0; i < NAMES.length; i++) {
                entities.add(getEntity(i));
            }
            when(queryResult.getReturnValue()).thenReturn(entities);
        } else {
            if (failure instanceof String) {
                when(queryResult.getExceptionString()).thenReturn((String) failure);
                setUpL10nExpectations((String) failure);
            } else if (failure instanceof Exception) {
                when(backend.runQuery(eq(VdcQueryType.GetGlusterVolumeBricks), any(IdQueryParameters.class))).thenThrow((Exception) failure);
                return;
            }
        }
        when(backend.runQuery(eq(VdcQueryType.GetGlusterVolumeBricks), any(IdQueryParameters.class))).thenReturn(
                queryResult);
    }

    private void setUpGetEntityExpectationsAllContent(boolean notFound) throws Exception {
        List<String> populateValue = new ArrayList<>();
        populateValue.add("true");
        when(httpHeaders.getRequestHeader(AbstractBackendResource.POPULATE)).thenReturn(populateValue);

        setupEntityExpectationAdvancedDetails(NAMES.length, notFound);
    }

    private void setupEntityExpectationAdvancedDetails(int times, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetGlusterVolumeById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { volumeId },
                    helper.getVolumeEntity(0));

            setUpGetEntityExpectations(VdcQueryType.GetGlusterVolumeAdvancedDetails,
                    GlusterVolumeAdvancedDetailsParameters.class,
                    new String[] { "ClusterId", "VolumeId", "BrickId", "DetailRequired" },
                    new Object[] { clusterId, volumeId, GUIDS[times], true },
                    notFound ? null : helper.getVolumeAdvancedDetailsEntity());
        }
    }

    private UriInfo setUpActionsUriExpectations() {
        UriInfo uriInfo = setUpBasicUriExpectations(MIGRATE_BRICKS_ACTION_BASE_URL);
        return uriInfo;
    }
}
