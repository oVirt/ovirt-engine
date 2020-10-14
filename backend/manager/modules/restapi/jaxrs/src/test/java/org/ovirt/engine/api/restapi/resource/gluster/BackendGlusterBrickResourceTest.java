package org.ovirt.engine.api.restapi.resource.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.gluster.GlusterTestHelper.brickId;
import static org.ovirt.engine.api.restapi.resource.gluster.GlusterTestHelper.clusterId;
import static org.ovirt.engine.api.restapi.resource.gluster.GlusterTestHelper.serverId;
import static org.ovirt.engine.api.restapi.resource.gluster.GlusterTestHelper.volumeId;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.Fault;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeAdvancedDetailsParameters;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendGlusterBrickResourceTest extends AbstractBackendSubResourceTest<GlusterBrick, GlusterBrickEntity, BackendGlusterBrickResource> {

    private BackendGlusterVolumeResource volumeResourceMock;
    private BackendGlusterBricksResource bricksResourceMock;
    private GlusterTestHelper helper;

    public BackendGlusterBrickResourceTest() {
        super(new BackendGlusterBrickResource(brickId.toString()));
    }

    @Override
    protected void init() {
        super.init();
        helper = new GlusterTestHelper();
        bricksResourceMock = mock(BackendGlusterBricksResource.class);
    }

    @Test
    public void testGet() {
        setupParentExpectations();
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        resource.setParent(bricksResourceMock);

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testReplace() {
        WebApplicationException wae = assertThrows(WebApplicationException.class,  () -> resource.replace(new Action()));
        assertTrue(wae.getResponse().getEntity() instanceof Fault);
    }

    @Test
    public void testPopulate() {
        setupParentExpectations();
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectationsAllContent(1, false);

        resource.setParent(bricksResourceMock);

        verifyModelWithDetails(resource.get(), 0);
    }

    @Test
    public void testPopulateNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectationsAllContent(1, true);

        resource.setParent(bricksResourceMock);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testStatisticalQuery() {

        setUriInfo(setUpBasicUriExpectations());
        setupParentExpectations();

        setupEntityExpectationAdvancedDetails(1 , false, true);

        resource.setParent(bricksResourceMock);
        StatisticsResource statRes = resource.getStatisticsResource();
        Statistics statistics = statRes.list();
        assertNotNull(statistics);
    }

    /**
     * This test only checks that the {@code remove method} calls the {@code remove} method of the parent, the actual
     * test of that logic is part of the parent tests.
     */
    @Test
    public void testRemove() {
        resource.setParent(bricksResourceMock);
        setupParentExpectations();
        setUpGetEntityExpectations(1);
        setUpCallParentRemoveExpectations();
        verifyRemove(resource.remove());
    }

    private void setUpCallParentRemoveExpectations() {
        when(bricksResourceMock.remove(any())).thenReturn(Response.ok().build());
    }

    @Override
    protected GlusterBrickEntity getEntity(int index) {
        return helper.getBrickEntity(index, false);
    }

    private GlusterBrickEntity getEntityWithDetails(int index) {
        return helper.getBrickEntity(index, true);
    }

    /**
     * Overridden as {@link GlusterBrickEntity} does not have description field
     */
    @Override
    protected void verifyModel(GlusterBrick model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(serverId.toString(), model.getServerId());
        assertEquals(GlusterTestHelper.brickDir, model.getBrickDir());
        verifyLinks(model);
    }

    private void verifyModelWithDetails(GlusterBrick model, int index) {
        verifyModel(model, index);
        assertEquals(GlusterTestHelper.BRICK_PORT, model.getPort());
        assertEquals(GlusterTestHelper.BRICK_MNT_OPT, model.getMntOptions());
    }

    protected void setUpGetEntityExpectations(int times) {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetGlusterBrickById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { brickId },
                    notFound ? null : getEntity(0));
        }
    }

    private void setUpGetEntityExpectationsAllContent(int times, boolean notFound) {
        setUpGetEntityExpectations(times, notFound);
        List<String> populateValue = new ArrayList<>();
        populateValue.add("true");
        when(httpHeaders.getRequestHeader(AbstractBackendResource.ALL_CONTENT_HEADER)).thenReturn(populateValue);
        setupParentPopulateExpectations();
    }

    private void setupEntityExpectationAdvancedDetails(int times, boolean notFound, boolean hasBrickDetails) {
        // the brick entity should be returned. We are not testing for not found on that.
        //setUpGetEntityExpectations(times,false);
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetGlusterBrickById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { brickId },
                    hasBrickDetails ? getEntityWithDetails(0) : getEntity(0));
            setUpGetEntityExpectations(QueryType.GetGlusterVolumeById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { volumeId },
                    helper.getVolumeEntity(0));

            setUpGetEntityExpectations(QueryType.GetGlusterVolumeAdvancedDetails,
                    GlusterVolumeAdvancedDetailsParameters.class,
                    new String[] { "ClusterId", "VolumeId", "BrickId", "DetailRequired" },
                    new Object[] { clusterId, volumeId, brickId, true },
                    notFound ? null : helper.getVolumeAdvancedDetailsEntity());
        }
    }

    private void setupParentPopulateExpectations() {
        doAnswer(invocation -> {
            GlusterBrick model = (GlusterBrick) invocation.getArguments()[0];
            model.setPort(GlusterTestHelper.BRICK_PORT);
            model.setMntOptions(GlusterTestHelper.BRICK_MNT_OPT);
            return model;

        }).when(bricksResourceMock).populateAdvancedDetails(isA(GlusterBrick.class), isA(GlusterBrickEntity.class));
    }


    private void setupParentExpectations() {
        volumeResourceMock = mock(BackendGlusterVolumeResource.class);
        when(bricksResourceMock.getParent()).thenReturn(volumeResourceMock);
        when(volumeResourceMock.getId()).thenReturn(volumeId.toString());

        doAnswer(invocation -> {
            GlusterBrick glusterBrick = (GlusterBrick) invocation.getArguments()[0];
            Cluster cluster = new Cluster();
            cluster.setId(clusterId.toString());

            GlusterVolume volume = new GlusterVolume();
            volume.setId(volumeId.toString());
            volume.setCluster(cluster);
            glusterBrick.setGlusterVolume(volume);
            return glusterBrick;

        }).when(bricksResourceMock).addParents(isA(GlusterBrick.class));
    }
}
