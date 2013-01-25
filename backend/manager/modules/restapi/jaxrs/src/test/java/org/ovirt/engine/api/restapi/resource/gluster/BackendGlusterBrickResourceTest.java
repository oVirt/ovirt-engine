package org.ovirt.engine.api.restapi.resource.gluster;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.Statistics;
import org.ovirt.engine.api.resource.StatisticsResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeReplaceBrickActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.MallInfo;
import org.ovirt.engine.core.common.businessentities.gluster.MemoryStatus;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeAdvancedDetailsParameters;
import org.ovirt.engine.core.compat.Guid;


public class BackendGlusterBrickResourceTest extends AbstractBackendSubResourceTest<GlusterBrick, GlusterBrickEntity, BackendGlusterBrickResource> {

    private static final Guid volumeId = GlusterTestHelper.volumeId;//GUIDS[1];
    private static final Guid clusterId = GlusterTestHelper.clusterId;//GUIDS[2];
    private static final Guid serverId = GlusterTestHelper.serverId; //GUIDS[3];
    private static final Guid brickId = GUIDS[3];

    private BackendGlusterVolumeResource volumeResourceMock;
    private BackendGlusterBricksResource bricksResourceMock;

    public BackendGlusterBrickResourceTest() {
        super(new BackendGlusterBrickResource(brickId.toString()));
    }

    @Override
    protected void init() {
        super.init();
    }

    @Test
    public void testGet() throws Exception {
        setupParentExpectations();
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        resource.setParent(bricksResourceMock);
        control.replay();

        verifyModel(resource.get(), 0);
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
    public void testReplaceInvalidParams() throws Exception {
        setupParentExpectations();
        setUriInfo(setUpBasicUriExpectations());
        control.replay();

        try {
            resource.replace(new Action());
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Action", "replace", "Brick.serverId, Brick.brickDir");
       }
    }

    @Test
    public void testReplace() throws Exception {
        setupParentExpectations();
        setUpGetEntityExpectations(1);
        setUriInfo(setUpActionExpectations(VdcActionType.ReplaceGlusterVolumeBrick,
                GlusterVolumeReplaceBrickActionParameters.class,
                new String[] { "VolumeId", "NewBrick.ServerId", "NewBrick.BrickDirectory" },
                new Object[] { volumeId, serverId, GlusterTestHelper.brickDir }));
        resource.setParent(bricksResourceMock);

        Action action = new Action();
        action.setBrick(new GlusterBrick());
        action.getBrick().setServerId(serverId.toString());
        action.getBrick().setBrickDir(GlusterTestHelper.brickDir);

        verifyActionResponse(resource.replace(action));
    }

    @Test
    public void testPopulate() throws Exception {
        setupParentExpectations();
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectationsAllContent(1,false);

        resource.setParent(bricksResourceMock);
        control.replay();

        verifyModelWithDetails(resource.get(), 0);
    }

    @Test
    public void testPopulateNotFound() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectationsAllContent(1,true);

        resource.setParent(bricksResourceMock);
        control.replay();
        try {
            resource.get();
            fail("expected WebApplicationException");
        }catch (WebApplicationException ex) {
            verifyNotFoundException(ex);
        }

    }

    @Test
    public void testStatisticalQuery() throws Exception {

        setUriInfo(setUpBasicUriExpectations());
        setupParentExpectations();

        setupEntityExpectationAdvancedDetails(1 , false, true);

        resource.setParent(bricksResourceMock);
        control.replay();
        StatisticsResource statRes = resource.getStatisticsResource();
        Statistics statistics = statRes.list();
        assertNotNull(statistics);
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz,
            String[] names,
            Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "bricks/" + brickId, false);
    }

    @Override
    protected GlusterBrickEntity getEntity(int index) {
        return setUpEntityExpectations(control.createMock(GlusterBrickEntity.class), index, false);
    }

    private GlusterBrickEntity setUpEntityExpectations(
            GlusterBrickEntity entity, int index, boolean hasDetails) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getServerId()).andReturn(serverId).anyTimes();
        expect(entity.getBrickDirectory()).andReturn(GlusterTestHelper.brickDir).anyTimes();
        expect(entity.getQualifiedName()).andReturn(GlusterTestHelper.brickName).anyTimes();
        expect(entity.getVolumeId()).andReturn(volumeId).anyTimes();
        if (hasDetails){
            BrickDetails brickDetails = control.createMock(BrickDetails.class);
            BrickProperties brickProps = control.createMock(BrickProperties.class);
            MemoryStatus memStatus = control.createMock(MemoryStatus.class);
            MallInfo mallInfo = control.createMock(MallInfo.class);
            expect(mallInfo.getArena()).andReturn(888);
            expect(brickProps.getMntOptions()).andReturn(GlusterTestHelper.BRICK_MNT_OPT).anyTimes();
            expect(brickProps.getPort()).andReturn(GlusterTestHelper.BRICK_PORT).anyTimes();
            expect(brickDetails.getMemoryStatus()).andReturn(memStatus);
            expect(memStatus.getMallInfo()).andReturn(mallInfo);
            expect(brickDetails.getBrickProperties()).andReturn(brickProps).anyTimes();
            //List<BrickDetails> brickDetailsList = Arrays.asList(brickDetails);
            expect(entity.getBrickDetails()).andReturn(brickDetails).anyTimes();
      }

        return entity;
    }

    private GlusterBrickEntity getEntityWithDetails(int index) {
        return setUpEntityExpectations(control.createMock(GlusterBrickEntity.class), index, true);
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

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetGlusterBrickById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { brickId },
                    notFound ? null : getEntity(0));
        }
    }

    private void setUpGetEntityExpectationsAllContent(int times, boolean notFound) throws Exception {
        List<String> populateValue = new ArrayList<String>();
        populateValue.add("true");
        expect(httpHeaders.getRequestHeader(AbstractBackendResource.POPULATE)).andReturn(populateValue).anyTimes();

        setupEntityExpectationAdvancedDetails(times, notFound, false);
    }

    private void setupEntityExpectationAdvancedDetails(int times, boolean notFound, boolean hasBrickDetails) throws Exception {
        // the brick entity should be returned. We are not testing for not found on that.
        //setUpGetEntityExpectations(times,false);
        GlusterTestHelper helper = new GlusterTestHelper(control);
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetGlusterBrickById,
                IdQueryParameters.class,
                new String[] { "Id" },
                new Object[] { brickId },
                hasBrickDetails? getEntityWithDetails(0): getEntity(0));
            setUpGetEntityExpectations(VdcQueryType.GetGlusterVolumeById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { volumeId },
                    helper.getVolumeEntity(0));

            setUpGetEntityExpectations(VdcQueryType.GetGlusterVolumeAdvancedDetails,
                    GlusterVolumeAdvancedDetailsParameters.class,
                    new String[] { "ClusterId", "VolumeName", "BrickName", "DetailRequired" },
                    new Object[] { clusterId, GlusterTestHelper.volumeName, GlusterTestHelper.brickName, true },
                    notFound ? null : helper.getVolumeAdvancedDetailsEntity(0));
        }
    }


    private void setupParentExpectations() {


        bricksResourceMock = control.createMock(BackendGlusterBricksResource.class);
        volumeResourceMock = control.createMock(BackendGlusterVolumeResource.class);
        expect(bricksResourceMock.getParent()).andReturn(volumeResourceMock).anyTimes();
        expect(volumeResourceMock.getId()).andReturn(volumeId.toString()).anyTimes();

        expect(bricksResourceMock.addParents(isA(GlusterBrick.class))).andDelegateTo(
                new BackendGlusterBricksResource() {

                    @Override
                    protected GlusterBrick addParents(GlusterBrick glusterBrick) {
                        Cluster cluster = new Cluster();
                        cluster.setId(clusterId.toString());

                        GlusterVolume volume = new GlusterVolume();
                        volume.setId(volumeId.toString());
                        volume.setCluster(cluster);
                        glusterBrick.setGlusterVolume(volume);
                        return glusterBrick;
                    }

                }).anyTimes();

    }


}
