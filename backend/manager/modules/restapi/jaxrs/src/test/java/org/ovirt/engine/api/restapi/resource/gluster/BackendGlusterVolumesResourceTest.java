package org.ovirt.engine.api.restapi.resource.gluster;

import static org.easymock.EasyMock.expect;

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
import org.ovirt.engine.api.restapi.resource.BackendResource;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendGlusterVolumesResourceTest extends AbstractBackendCollectionResourceTest<GlusterVolume, GlusterVolumeEntity, BackendGlusterVolumesResource> {
    private static final Guid clusterId = GUIDS[0];
    private static final Guid volumeId = GUIDS[1];
    private static final Guid serverId = GUIDS[2];
    private static final String defaultClusterName = "Default";
    private static final String defaultQuery = "cluster = " + defaultClusterName;
    private static final String volumesQueryPrefix = "Volumes : ";
    private static ClusterResource parentMock;

    public BackendGlusterVolumesResourceTest() {
        super(new BackendGlusterVolumesResource(parentMock, clusterId.toString()),
                SearchType.GlusterVolume,
                volumesQueryPrefix);
    }

    /**
     * Override init to perform additional mocking required
     * for the "list" method of the collection resource.
     */
    @Override
    protected void init() {
        super.init();
        setupListExpectations();
    }

    /**
     * Overridden to set query expectations for the default query (cluster = Default),
     * as the gluster volumes collection resource always fetches volumes of a given
     * cluster (it is not a root level collection).
     */
    @Override
    public void testList() throws Exception {
        collection.setUriInfo(setUpUriExpectations(null));
        setUpQueryExpectations(defaultQuery);
        collection.setParent(parentMock);
        verifyCollection(getCollection());
    }

    @Override
    public void testQuery() throws Exception {
        collection.setUriInfo(setUpUriExpectations(QUERY));
        setUpQueryExpectations(QUERY);
        collection.setParent(parentMock);
        verifyCollection(getCollection());
    }

    /**
     * Refer comments on {@link #testList()}
     */
    @Override
    public void testListCrash() throws Exception {
        UriInfo uriInfo = setUpUriExpectations(null);
        collection.setUriInfo(uriInfo);

        Throwable t = new RuntimeException(FAILURE);
        setUpQueryExpectations(defaultQuery, t);

        collection.setParent(parentMock);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_SERVER_LOCALE, t);
        }
    }

    /**
     * Refer comments on {@link #testList()}
     */
    @Override
    public void testListCrashClientLocale() throws Exception {
        collection.setUriInfo(setUpUriExpectations(null));
        locales.add(CLIENT_LOCALE);

        Throwable t = new RuntimeException(FAILURE);
        setUpQueryExpectations(defaultQuery, t);

        collection.setParent(parentMock);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            verifyFault(wae, BACKEND_FAILED_CLIENT_LOCALE, t);
        } finally {
            locales.clear();
        }
    }

    /**
     * Refer comments on {@link #testList()}
     */
    @Override
    public void testListFailure() throws Exception {
        collection.setUriInfo(setUpUriExpectations(null));

        setUpQueryExpectations(defaultQuery, FAILURE);

        collection.setParent(parentMock);
        try {
            getCollection();
            fail("expected WebApplicationException");
        } catch (WebApplicationException wae) {
            assertTrue(wae.getResponse().getEntity() instanceof Fault);
            assertEquals(mockl10n(FAILURE), ((Fault) wae.getResponse().getEntity()).getDetail());
        }
    }

    @Override
    protected List<GlusterVolume> getCollection() {
        return collection.list().getGlusterVolumes();
    }

    @Override
    protected GlusterVolumeEntity getEntity(int index) {
        return setUpEntityExpectations(
                control.createMock(GlusterVolumeEntity.class),
                index);
    }

    @Test
    public void testAdd() {
        setUriInfo(setUpBasicUriExpectations());
        setUpVolumeCreationExpectations(false);

        collection.setParent(parentMock);
        Response response = collection.add(createModel());
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof GlusterVolume);
        verifyModel((GlusterVolume) response.getEntity(), 1);
    }

    @Test
    public void testAddForce() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        setUriInfo(setUpGetMatrixConstraintsExpectations(
                BackendResource.FORCE_CONSTRAINT,
                true,
                "true",
                uriInfo,
                false));
        setUpVolumeCreationExpectations(true);

        collection.setParent(parentMock);
        Response response = collection.add(createModel());
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof GlusterVolume);
        verifyModel((GlusterVolume) response.getEntity(), 1);
    }

    @Test
    public void testAddForceFalse() {
        UriInfo uriInfo = setUpBasicUriExpectations();
        setUriInfo(setUpGetMatrixConstraintsExpectations(
                BackendResource.FORCE_CONSTRAINT,
                false,
                "false",
                uriInfo,
                false));
        setUpVolumeCreationExpectations(false);

        collection.setParent(parentMock);
        Response response = collection.add(createModel());
        assertEquals(201, response.getStatus());
        assertTrue(response.getEntity() instanceof GlusterVolume);
        verifyModel((GlusterVolume) response.getEntity(), 1);
    }

    private void setUpVolumeCreationExpectations(boolean force) {
        setUpCreationExpectations(VdcActionType.CreateGlusterVolume,
                                  CreateGlusterVolumeParameters.class,
                new String[] { "Volume.Name", "Volume.VolumeType", "Force" },
                new Object[] { "testVol1", GlusterVolumeType.DISTRIBUTE, force },
                                  true,
                                  true,
                                  volumeId,
                                  VdcQueryType.GetGlusterVolumeById,
                                  IdQueryParameters.class,
                                  new String[] { "Id" },
                                  new Object[] { volumeId },
                                  getEntity(1));
    }

    /**
     * Overridden as {@link GlusterVolumeEntity} does not have description field
     */
    @Override
    protected void verifyModel(GlusterVolume model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(NAMES[index], model.getName());
        assertEquals(org.ovirt.engine.api.model.GlusterVolumeType.DISTRIBUTE, model.getVolumeType());
        assertEquals(clusterId.toString(), model.getCluster().getId());
        verifyLinks(model);
    }

    private GlusterVolume createModel() {
        GlusterVolume volume = new GlusterVolume();
        volume.setName("testVol1");
        volume.setCluster(new Cluster());
        volume.getCluster().setId(clusterId.toString());
        volume.setVolumeType(org.ovirt.engine.api.model.GlusterVolumeType.DISTRIBUTE);
        volume.setBricks(new GlusterBricks());
        volume.getBricks().getGlusterBricks().add(createBrick("/export/vol1/brick1"));
        return volume;
    }

    private GlusterBrick createBrick(String brickDir) {
        GlusterBrick brick = new GlusterBrick();
        brick.setServerId(serverId.toString());
        brick.setBrickDir(brickDir);
        return brick;
    }

    static GlusterVolumeEntity setUpEntityExpectations(
            GlusterVolumeEntity entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getName()).andReturn(NAMES[index]).anyTimes();
        expect(entity.getClusterId()).andReturn(GUIDS[2]).anyTimes();
        expect(entity.getVolumeType()).andReturn(GlusterVolumeType.DISTRIBUTE).anyTimes();
        return entity;
    }

    /**
     * The method {@link BackendGlusterVolumesResource#list()} internally
     * invokes {@link ClusterResource#get()} to fetch the cluster object,
     * and then invokes the query including the default constraint on cluster name.
     * This method mocks the cluster resource in such a way that the cluster
     * name will be returned as "Default"
     */
    private void setupListExpectations() {
        Cluster cluster = new Cluster();
        cluster.setName(defaultClusterName);
        cluster.setId(clusterId.toString());

        parentMock = control.createMock(ClusterResource.class);
        expect(parentMock.get()).andReturn(cluster).anyTimes();
    }
}
