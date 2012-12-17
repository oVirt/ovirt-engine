package org.ovirt.engine.api.restapi.resource.gluster;

import static org.easymock.EasyMock.expect;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeReplaceBrickActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendGlusterBrickResourceTest extends AbstractBackendSubResourceTest<GlusterBrick, GlusterBrickEntity, BackendGlusterBrickResource> {
    private static final Guid brickId = GUIDS[0];
    private static final Guid volumeId = GUIDS[1];
    private static final Guid clusterId = GUIDS[2];
    private static final Guid serverId = GUIDS[3];
    private static final String brickDir = "/export/vol1/brick1";
    private ClusterResource clusterResourceMock;
    private BackendGlusterVolumesResource volumesResourceMock;
    private BackendGlusterVolumeResource volumeResourceMock;
    private BackendGlusterBricksResource bricksResourceMock;

    public BackendGlusterBrickResourceTest() {
        super(new BackendGlusterBrickResource(brickId.toString()));
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
                new Object[] { volumeId, serverId, brickDir }));
        resource.setParent(bricksResourceMock);

        Action action = new Action();
        action.setBrick(new GlusterBrick());
        action.getBrick().setServerId(serverId.toString());
        action.getBrick().setBrickDir(brickDir);

        verifyActionResponse(resource.replace(action));
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
        return setUpEntityExpectations(control.createMock(GlusterBrickEntity.class), index);
    }

    private GlusterBrickEntity setUpEntityExpectations(
            GlusterBrickEntity entity, int index) {
        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getServerId()).andReturn(serverId).anyTimes();
        expect(entity.getBrickDirectory()).andReturn(brickDir).anyTimes();
        expect(entity.getVolumeId()).andReturn(volumeId).anyTimes();
        return entity;
    }

    /**
     * Overridden as {@link GlusterBrickEntity} does not have description field
     */
    @Override
    protected void verifyModel(GlusterBrick model, int index) {
        assertEquals(GUIDS[index].toString(), model.getId());
        assertEquals(serverId.toString(), model.getServerId());
        assertEquals(brickDir, model.getBrickDir());
        verifyLinks(model);
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

    private void setupParentExpectations() {
        Cluster cluster = new Cluster();
        cluster.setId(clusterId.toString());

        GlusterVolume volume = control.createMock(GlusterVolume.class);
        expect(volume.getId()).andReturn(volumeId.toString()).anyTimes();

        clusterResourceMock = control.createMock(ClusterResource.class);
        expect(clusterResourceMock.get()).andReturn(cluster).anyTimes();

        volumesResourceMock = control.createMock(BackendGlusterVolumesResource.class);
        expect(volumesResourceMock.getParent()).andReturn(clusterResourceMock).anyTimes();

        volumeResourceMock = control.createMock(BackendGlusterVolumeResource.class);
        expect(volumeResourceMock.get()).andReturn(volume).anyTimes();
        expect(volumeResourceMock.getParent()).andReturn(volumesResourceMock).anyTimes();

        bricksResourceMock = control.createMock(BackendGlusterBricksResource.class);
        expect(bricksResourceMock.getParent()).andReturn(volumeResourceMock).anyTimes();
    }
}
