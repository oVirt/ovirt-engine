package org.ovirt.engine.api.restapi.resource.gluster;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.ovirt.engine.api.restapi.resource.gluster.BackendGlusterVolumesResourceTest.setUpEntityExpectations;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.action.gluster.ResetGlusterVolumeOptionsParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendGlusterVolumeResourceTest extends AbstractBackendSubResourceTest<GlusterVolume, GlusterVolumeEntity, BackendGlusterVolumeResource> {
    private static final Guid clusterId = GUIDS[0];
    private static final String defaultClusterName = "Default";
    private ClusterResource clusterResourceMock;
    private BackendGlusterVolumesResource volumesResourceMock;

    public BackendGlusterVolumeResourceTest() {
        super(new BackendGlusterVolumeResource(GUIDS[0].toString()));
    }

    @Test
    public void testGet() throws Exception {
        setupParentExpectations();
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        resource.setParent(volumesResourceMock);
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
    public void testStart() throws Exception {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(VdcActionType.StartGlusterVolume,
                GlusterVolumeActionParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.start(new Action()));
    }

    @Test
    public void testStop() throws Exception {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(VdcActionType.StopGlusterVolume,
                GlusterVolumeActionParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.stop(new Action()));
    }

    @Test
    public void testRebalance() throws Exception {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(VdcActionType.StartRebalanceGlusterVolume,
                GlusterVolumeRebalanceParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.rebalance(new Action()));
    }

    @Test
    public void testStopRebalance() throws Exception {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(VdcActionType.StopRebalanceGlusterVolume,
                GlusterVolumeRebalanceParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.stopRebalance(new Action()));
    }

    @Test
    public void testSetOptionInvalidParams() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        resource.setUriInfo(setUpBasicUriExpectations());

        try {
            control.replay();
            Action action = new Action();
            action.setOption(new Option());
            resource.setOption(action);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Option", "setOption", "name, value");
        }
    }

    @Test
    public void testSetOption() throws Exception {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(VdcActionType.SetGlusterVolumeOption,
                GlusterVolumeOptionParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        Action action = new Action();
        action.setOption(new Option());
        action.getOption().setName("auth.allow");
        action.getOption().setValue("*");
        verifyActionResponse(resource.setOption(action));
    }

    @Test
    public void testResetOptionInvalidParams() throws Exception {
        setUriInfo(setUpBasicUriExpectations());
        resource.setUriInfo(setUpBasicUriExpectations());

        try {
            control.replay();
            Action action = new Action();
            action.setOption(new Option());
            resource.resetOption(action);
            fail("expected WebApplicationException on incomplete parameters");
        } catch (WebApplicationException wae) {
            verifyIncompleteException(wae, "Option", "resetOption", "name");
        }
    }

    @Test
    public void testResetOption() throws Exception {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(VdcActionType.ResetGlusterVolumeOptions,
                ResetGlusterVolumeOptionsParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        Action action = new Action();
        action.setOption(new Option());
        action.getOption().setName("auth.allow");
        verifyActionResponse(resource.resetOption(action));
    }

    @Test
    public void testResetAllOptions() throws Exception {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(VdcActionType.ResetGlusterVolumeOptions,
                ResetGlusterVolumeOptionsParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.resetAllOptions(new Action()));
    }

    @Test
    public void testRemove() throws Exception {
        setupParentExpectations();
        setUpGetEntityExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                VdcActionType.DeleteGlusterVolume,
                GlusterVolumeParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] },
                true,
                true
            )
        );
        resource.setParent(volumesResourceMock);
        verifyRemove(resource.remove());
    }

    protected UriInfo setUpActionExpectations(VdcActionType task,
            Class<? extends VdcActionParametersBase> clz,
            String[] names,
            Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    private void verifyActionResponse(Response r) throws Exception {
        verifyActionResponse(r, "glustervolumes/" + GUIDS[0], false);
    }

    @Override
    protected GlusterVolumeEntity getEntity(int index) {
        return setUpEntityExpectations(control.createMock(GlusterVolumeEntity.class), index);
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

    protected void setUpGetEntityExpectations(int times) throws Exception {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) throws Exception {
        while (times-- > 0) {
            setUpGetEntityExpectations(VdcQueryType.GetGlusterVolumeById,
                    IdQueryParameters.class,
                    new String[] { "Id" },
                    new Object[] { GUIDS[0] },
                    notFound ? null : getEntity(0));
        }
    }

    private void setupParentExpectations() {
        Cluster cluster = new Cluster();
        cluster.setName(defaultClusterName);
        cluster.setId(clusterId.toString());

        clusterResourceMock = control.createMock(ClusterResource.class);
        expect(clusterResourceMock.get()).andReturn(cluster).anyTimes();

        volumesResourceMock = control.createMock(BackendGlusterVolumesResource.class);
        expect(volumesResourceMock.getParent()).andReturn(clusterResourceMock).anyTimes();
        expect(volumesResourceMock.addParents(isA(GlusterVolume.class))).andDelegateTo(
                new BackendGlusterVolumesResource() {
                    @Override
                    protected GlusterVolume addParents(GlusterVolume model) {
                        Cluster cluster = new Cluster();
                        cluster.setId(clusterId.toString());
                        model.setCluster(cluster);
                        return model;
                    }
                }).anyTimes();
    }
}
