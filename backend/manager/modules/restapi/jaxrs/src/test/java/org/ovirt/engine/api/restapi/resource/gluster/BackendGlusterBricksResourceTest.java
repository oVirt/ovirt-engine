package org.ovirt.engine.api.restapi.resource.gluster;


import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterBricks;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResourceTest;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendGlusterBricksResourceTest extends AbstractBackendCollectionResourceTest<GlusterBrick, GlusterBrickEntity, BackendGlusterBricksResource> {
    private static final Guid clusterId = GUIDS[0];
    private static final Guid volumeId = GUIDS[1];
    private static final Guid serverId = GUIDS[2];
    private static final String serverName = "testServer";
    private static final String brickDir = "/tmp/vol1/brick1";
    private BackendGlusterVolumeResource parentMock;

    public BackendGlusterBricksResourceTest() {
        super(new BackendGlusterBricksResource(),
                null,
                null);
    }

    /**
     * Override init to perform additional mocking required
     * for the "list" method of the collection resource.
     */
    @Override
    protected void init() {
        super.init();
        setUpParentMocks();
    }

    @Override
    protected List<GlusterBrick> getCollection() {
        return collection.list().getGlusterBricks();
    }

    @Override
    protected GlusterBrickEntity getEntity(int index) {
        return setUpEntityExpectations(
                control.createMock(GlusterBrickEntity.class),
                index);
    }

    @Override
    @Test
    public void testList() throws Exception {
        setUpBricksQueryExpectations(null);
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);
        control.replay();

        verifyCollection(getCollection());
    }

    @Override
    @Test
    public void testListFailure() throws Exception {
        setUpBricksQueryExpectations(FAILURE);
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);
        control.replay();

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

        control.replay();

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

        control.replay();
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
    public void testAdd() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        expect(uriInfo.getPath()).andReturn("clusters/" + clusterId + "/glustervolumes/" + volumeId + "/bricks")
                .anyTimes();
        setUriInfo(uriInfo);
        setUpCreationExpectations(VdcActionType.AddBricksToGlusterVolume,
                                  GlusterVolumeBricksActionParameters.class,
                                  new String[] { "VolumeId", "Bricks" },
                                  new Object[] { volumeId, getBricks() },
                                  true,
                                  true,
                                  getBrickIds(),
                                  VdcQueryType.GetGlusterBrickById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { GUIDS[0] },
                                  getEntity(0));

        Response response = collection.add(createModel());
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof GlusterBricks);
        verifyModel(((GlusterBricks) response.getEntity()).getGlusterBricks().get(0), 0);
    }

    @Test
    public void testRemove() throws Exception {
        setUriInfo(setUpActionExpectations(VdcActionType.GlusterVolumeRemoveBricks,
                GlusterVolumeRemoveBricksParameters.class,
                                           new String[] { "VolumeId", "Bricks" },
                                           new Object[] { volumeId, getBricksToRemove() },
                                           true,
                                           true));
        verifyRemove(collection.remove(GUIDS[0].toString()));
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
        List<Guid> brickIds = new ArrayList<Guid>();
        brickIds.add(GUIDS[0]);
        return brickIds;
    }

    private List<GlusterBrickEntity> getBricksToRemove() {
        List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();
        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setId(GUIDS[0]);
        brick.setVolumeId(volumeId);
        bricks.add(brick);
        return bricks;
    }

    private List<GlusterBrickEntity> getBricks() {
        List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();
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

    static GlusterBrickEntity setUpEntityExpectations(
            GlusterBrickEntity entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getVolumeId()).andReturn(volumeId).anyTimes();
        expect(entity.getServerId()).andReturn(serverId).anyTimes();
        expect(entity.getServerName()).andReturn(serverName).anyTimes();
        expect(entity.getBrickDirectory()).andReturn(brickDir).anyTimes();
        return entity;
    }

    /**
     * The method {@link BackendGlusterBricksResource#list()} internally
     * invokes {@link BackendGlusterVolumeResource#get()} to fetch the volume object,
     * and then invokes the query to fetch the bricks of that volume.
     * This method mocks the volume resource to return pre-defined volume id
     */
    private void setUpParentMocks() {
        GlusterVolume volume = new GlusterVolume();
        volume.setId(volumeId.toString());

        Cluster cluster = new Cluster();
        cluster.setId(clusterId.toString());
        volume.setCluster(cluster);

        ClusterResource clusterResourceMock = control.createMock(ClusterResource.class);
        expect(clusterResourceMock.get()).andReturn(cluster).anyTimes();

        BackendGlusterVolumesResource volumesResourceMock = control.createMock(BackendGlusterVolumesResource.class);
        expect(volumesResourceMock.getParent()).andReturn(clusterResourceMock).anyTimes();

        parentMock = control.createMock(BackendGlusterVolumeResource.class);
        expect(parentMock.getParent()).andReturn(volumesResourceMock).anyTimes();
        expect(parentMock.get()).andReturn(volume).anyTimes();

        collection.setParent(parentMock);
    }

    private void setUpBricksQueryExpectations(Object failure) {
        VdcQueryReturnValue queryResult = control.createMock(VdcQueryReturnValue.class);
        expect(queryResult.getSucceeded()).andReturn(failure == null).anyTimes();
        List<GlusterBrickEntity> entities = new ArrayList<GlusterBrickEntity>();

        if (failure == null) {
            for (int i = 0; i < NAMES.length; i++) {
                entities.add(getEntity(i));
            }
            expect(queryResult.getReturnValue()).andReturn(entities).anyTimes();
        } else {
            if (failure instanceof String) {
                expect(queryResult.getExceptionString()).andReturn((String) failure).anyTimes();
                setUpL10nExpectations((String)failure);
            } else if (failure instanceof Exception) {
                expect(backend.RunQuery(eq(VdcQueryType.GetGlusterVolumeBricks), anyObject(IdQueryParameters.class))).andThrow((Exception) failure).anyTimes();
                return;
            }
        }
        expect(backend.RunQuery(eq(VdcQueryType.GetGlusterVolumeBricks), anyObject(IdQueryParameters.class))).andReturn(
                queryResult);
    }
}
