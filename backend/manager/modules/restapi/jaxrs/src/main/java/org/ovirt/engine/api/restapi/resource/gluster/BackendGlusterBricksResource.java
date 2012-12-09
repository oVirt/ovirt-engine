package org.ovirt.engine.api.restapi.resource.gluster;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterBricks;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.resource.gluster.GlusterBrickResource;
import org.ovirt.engine.api.resource.gluster.GlusterBricksResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeBricksActionParameters;
import org.ovirt.engine.core.common.action.gluster.GlusterVolumeRemoveBricksParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendGlusterBricksResource
        extends AbstractBackendCollectionResource<GlusterBrick, GlusterBrickEntity>
        implements GlusterBricksResource {

    private BackendGlusterVolumeResource parent;

    public BackendGlusterBricksResource() {
        super(GlusterBrick.class, GlusterBrickEntity.class);
    }

    public BackendGlusterBricksResource(BackendGlusterVolumeResource parent) {
        super(GlusterBrick.class, GlusterBrickEntity.class);
        setParent(parent);
    }

    @SuppressWarnings("unchecked")
    @Override
    public GlusterBricks list() {
        List<GlusterBrickEntity> bricks =
                getBackendCollection(VdcQueryType.GetGlusterVolumeBricks, new IdQueryParameters(asGuid(getVolumeId())));
        return mapCollection(bricks);
    }

    private GlusterBricks mapCollection(List<GlusterBrickEntity> entities) {
        GlusterBricks collection = new GlusterBricks();
        for (GlusterBrickEntity entity : entities) {
            collection.getGlusterBricks().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected GlusterBrick addParents(GlusterBrick glusterBrick) {
        GlusterVolume volume = new GlusterVolume();
        volume.setId(getVolumeId());

        Cluster cluster = new Cluster();
        cluster.setId(getClusterId());
        volume.setCluster(cluster);

        glusterBrick.setGlusterVolume(volume);
        return glusterBrick;
    }

    private String getClusterId() {
        return parent.getParent().getParent().get().getId();
    }

    private List<GlusterBrickEntity> mapBricks(Guid volumeId, GlusterBricks glusterBricks) {
        List<GlusterBrickEntity> bricks = new ArrayList<GlusterBrickEntity>();
        if (glusterBricks.getGlusterBricks().size() > 0) {
            for (GlusterBrick brick : glusterBricks.getGlusterBricks()) {
                GlusterBrickEntity brickEntity =
                        getMapper(GlusterBrick.class, GlusterBrickEntity.class).map(brick, null);
                brickEntity.setVolumeId(volumeId);
                bricks.add(brickEntity);
            }
        }
        return bricks;
    }

    @Override
    public Response add(GlusterBricks bricks) {
        for (GlusterBrick brick : bricks.getGlusterBricks()) {
            validateParameters(brick, "serverId", "brickDir");
        }

        List<GlusterBrickEntity> brickEntities = mapBricks(asGuid(getVolumeId()), bricks);
        int replicaCount = bricks.isSetReplicaCount() ? bricks.getReplicaCount() : 0;
        int stripeCount = bricks.isSetStripeCount() ? bricks.getStripeCount() : 0;

        return performCreationMultiple(VdcActionType.AddBricksToGlusterVolume,
                new GlusterVolumeBricksActionParameters(asGuid(getVolumeId()), brickEntities, replicaCount, stripeCount),
                new QueryIdResolver<Guid>(VdcQueryType.GetGlusterBrickById, IdQueryParameters.class));
    }

    private String getVolumeId() {
        return parent.get().getId();
    }

    @SuppressWarnings("unchecked")
    protected GlusterBricks resolveCreatedList(VdcReturnValueBase result, EntityIdResolver<Guid> entityResolver) {
        try {
            GlusterBricks bricks = new GlusterBricks();
            for(Guid id : (List<Guid>)result.getActionReturnValue()) {
                GlusterBrickEntity created = entityResolver.resolve(id);
                bricks.getGlusterBricks().add(addLinks(doPopulate(map(created), created)));
            }
            return bricks;
        } catch (Exception e) {
            // we tolerate a failure in the entity resolution
            // as the substantive action (entity creation) has
            // already succeeded
            e.printStackTrace();
            return null;
        }
    }

    protected Response performCreationMultiple(VdcActionType task,
            VdcActionParametersBase taskParams,
            EntityIdResolver<Guid> entityResolver) {
        VdcReturnValueBase createResult;
        try {
            createResult = doAction(task, taskParams);
        } catch (Exception e) {
            return handleError(e, false);
        }

        GlusterBricks model = resolveCreatedList(createResult, entityResolver);
        Response response = null;
        if (model == null) {
            response = Response.status(ACCEPTED_STATUS).build();
        } else {
            response =
                    Response.created(URI.create(getUriInfo().getPath())).entity(model).build();
        }
        return response;
    }

    @Override
    public Response remove(GlusterBricks bricks) {
        if (bricks.getGlusterBricks().size() > 0) {
            for (GlusterBrick brick : bricks.getGlusterBricks()) {
                validateParameters(brick, "id");
            }
        }
        int replicaCount = bricks.isSetReplicaCount() ? bricks.getReplicaCount() : 0;
        return performAction(VdcActionType.GlusterVolumeRemoveBricks,
                new GlusterVolumeRemoveBricksParameters(asGuid(getVolumeId()), mapBricks(asGuid(getVolumeId()), bricks), replicaCount));
    }

    @Override
    public GlusterBrickResource getGlusterBrickSubResource(String brickId) {
        return inject(new BackendGlusterBrickResource(brickId, this));
    }

    public BackendGlusterVolumeResource getParent() {
        return parent;
    }

    public void setParent(BackendGlusterVolumeResource parent) {
        this.parent = parent;
    }

    @Override
    protected Response performRemove(String id) {
        GlusterBrick brick = new GlusterBrick();
        brick.setId(id);
        GlusterBricks bricks = new GlusterBricks();
        bricks.getGlusterBricks().add(brick);
        return remove(bricks);
    }

    @Override
    protected GlusterBrick doPopulate(GlusterBrick model, GlusterBrickEntity entity) {
        return model;
    }

}
