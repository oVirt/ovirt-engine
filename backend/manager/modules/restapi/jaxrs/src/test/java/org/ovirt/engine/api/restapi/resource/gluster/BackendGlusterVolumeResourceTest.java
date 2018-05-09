package org.ovirt.engine.api.restapi.resource.gluster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.api.restapi.resource.gluster.BackendGlusterVolumesResourceTest.setUpEntityExpectations;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.jupiter.api.Test;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.Option;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendSubResourceTest;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeOptionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRebalanceParameters;
import org.ovirt.engine.core.common.action.gluster.ResetGlusterVolumeOptionsParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

@MockitoSettings(strictness = Strictness.LENIENT)
public class BackendGlusterVolumeResourceTest extends AbstractBackendSubResourceTest<GlusterVolume, GlusterVolumeEntity, BackendGlusterVolumeResource> {
    private static final Guid clusterId = GUIDS[0];
    private static final String defaultClusterName = "Default";
    private ClusterResource clusterResourceMock;
    private BackendGlusterVolumesResource volumesResourceMock;

    public BackendGlusterVolumeResourceTest() {
        super(new BackendGlusterVolumeResource(GUIDS[0].toString()));
    }

    @Test
    public void testGet() {
        setupParentExpectations();
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1);
        resource.setParent(volumesResourceMock);

        verifyModel(resource.get(), 0);
    }

    @Test
    public void testGetNotFound() {
        setUriInfo(setUpBasicUriExpectations());
        setUpGetEntityExpectations(1, true);
        verifyNotFoundException(assertThrows(WebApplicationException.class, resource::get));
    }

    @Test
    public void testStart() {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(ActionType.StartGlusterVolume,
                GlusterVolumeActionParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.start(new Action()));
    }

    @Test
    public void testStop() {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(ActionType.StopGlusterVolume,
                GlusterVolumeActionParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.stop(new Action()));
    }

    @Test
    public void testRebalance() {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(ActionType.StartRebalanceGlusterVolume,
                GlusterVolumeRebalanceParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.rebalance(new Action()));
    }

    @Test
    public void testStopRebalance() {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(ActionType.StopRebalanceGlusterVolume,
                GlusterVolumeRebalanceParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.stopRebalance(new Action()));
    }

    @Test
    public void testSetOptionInvalidParams() {
        setUriInfo(setUpBasicUriExpectations());
        resource.setUriInfo(setUpBasicUriExpectations());

        Action action = new Action();
        action.setOption(new Option());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> resource.setOption(action)),
                "Option", "setOption", "name, value");
    }

    @Test
    public void testSetOption() {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(ActionType.SetGlusterVolumeOption,
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
    public void testResetOptionInvalidParams() {
        setUriInfo(setUpBasicUriExpectations());
        resource.setUriInfo(setUpBasicUriExpectations());

        Action action = new Action();
        action.setOption(new Option());
        verifyIncompleteException(
                assertThrows(WebApplicationException.class, () -> resource.resetOption(action)),
                "Option", "resetOption", "name");
    }

    @Test
    public void testResetOption() {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(ActionType.ResetGlusterVolumeOptions,
                ResetGlusterVolumeOptionsParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        Action action = new Action();
        action.setOption(new Option());
        action.getOption().setName("auth.allow");
        verifyActionResponse(resource.resetOption(action));
    }

    @Test
    public void testResetAllOptions() {
        setupParentExpectations();
        resource.setParent(volumesResourceMock);
        setUriInfo(setUpActionExpectations(ActionType.ResetGlusterVolumeOptions,
                ResetGlusterVolumeOptionsParameters.class,
                new String[] { "VolumeId" },
                new Object[] { GUIDS[0] }));

        verifyActionResponse(resource.resetAllOptions(new Action()));
    }

    @Test
    public void testRemove() {
        setupParentExpectations();
        setUpGetEntityExpectations(1);
        setUriInfo(
            setUpActionExpectations(
                ActionType.DeleteGlusterVolume,
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

    protected UriInfo setUpActionExpectations(ActionType task,
            Class<? extends ActionParametersBase> clz,
            String[] names,
            Object[] values) {
        return setUpActionExpectations(task, clz, names, values, true, true, null, null, true);
    }

    private void verifyActionResponse(Response r) {
        verifyActionResponse(r, "glustervolumes/" + GUIDS[0], false);
    }

    @Override
    protected GlusterVolumeEntity getEntity(int index) {
        return setUpEntityExpectations(mock(GlusterVolumeEntity.class), index);
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

    protected void setUpGetEntityExpectations(int times) {
        setUpGetEntityExpectations(times, false);
    }

    protected void setUpGetEntityExpectations(int times, boolean notFound) {
        while (times-- > 0) {
            setUpGetEntityExpectations(QueryType.GetGlusterVolumeById,
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

        clusterResourceMock = mock(ClusterResource.class);
        when(clusterResourceMock.get()).thenReturn(cluster);

        volumesResourceMock = mock(BackendGlusterVolumesResource.class);
        when(volumesResourceMock.getParent()).thenReturn(clusterResourceMock);
        doAnswer(invocation -> {
            GlusterVolume model = (GlusterVolume) invocation.getArguments()[0];
            Cluster clusterModel = new Cluster();
            clusterModel.setId(clusterId.toString());
            model.setCluster(clusterModel);
            return model;
        }).when(volumesResourceMock).addParents(isA(GlusterVolume.class));
    }
}
