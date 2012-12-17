package org.ovirt.engine.api.restapi.resource.gluster;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.resource.gluster.GlusterBrickResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendActionableResource;
import org.ovirt.engine.api.restapi.types.Mapper;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeReplaceBrickActionParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskOperation;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;

public class BackendGlusterBrickResource
        extends AbstractBackendActionableResource<GlusterBrick, GlusterBrickEntity>
        implements GlusterBrickResource {
    private BackendGlusterBricksResource parent;

    public BackendGlusterBrickResource(String brickId, BackendGlusterBricksResource parent) {
        this(brickId);
        setParent(parent);
    }

    public BackendGlusterBrickResource(String brickId) {
        super(brickId, GlusterBrick.class, GlusterBrickEntity.class);
    }

    @Override
    public GlusterBrick get() {
        return performGet(VdcQueryType.GetGlusterBrickById, new IdQueryParameters(guid));
    }

    @Override
    protected GlusterBrick addParents(GlusterBrick model) {
        GlusterVolume volume = new GlusterVolume();
        volume.setId(getVolumeId());

        Cluster cluster = new Cluster();
        cluster.setId(getClusterId());
        volume.setCluster(cluster);

        model.setGlusterVolume(volume);
        return model;
    }

    protected String getClusterId() {
        return getParent().getParent().getParent().getParent().get().getId();
    }

    protected String getVolumeId() {
        return getParent().getParent().get().getId();
    }

    @Override
    public Response replace(Action action) {
        validateParameters(action, "Brick.serverId", "Brick.brickDir");
        Mapper<GlusterBrick, GlusterBrickEntity> mapper = getMapper(GlusterBrick.class, GlusterBrickEntity.class);
        return doAction(VdcActionType.ReplaceGlusterVolumeBrick,
                new GlusterVolumeReplaceBrickActionParameters(asGuid(getVolumeId()),
                        GlusterTaskOperation.START,
                        mapper.map(get(), null),
                        mapper.map(action.getBrick(), null),
                        action.isSetForce() ? action.isForce() : false),
                action);
    }

    public BackendGlusterBricksResource getParent() {
        return parent;
    }

    public void setParent(BackendGlusterBricksResource parent) {
        this.parent = parent;
    }
}
