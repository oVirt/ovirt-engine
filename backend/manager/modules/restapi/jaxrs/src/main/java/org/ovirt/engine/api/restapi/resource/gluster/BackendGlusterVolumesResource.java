package org.ovirt.engine.api.restapi.resource.gluster;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Cluster;
import org.ovirt.engine.api.model.GlusterBrick;
import org.ovirt.engine.api.model.GlusterVolume;
import org.ovirt.engine.api.model.GlusterVolumes;
import org.ovirt.engine.api.resource.ClusterResource;
import org.ovirt.engine.api.resource.gluster.GlusterVolumeResource;
import org.ovirt.engine.api.resource.gluster.GlusterVolumesResource;
import org.ovirt.engine.api.restapi.resource.AbstractBackendCollectionResource;
import org.ovirt.engine.api.restapi.util.QueryHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.gluster.CreateGlusterVolumeParameters;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;

/**
 * Implementation of the "glustervolumes" resource
 */
public class BackendGlusterVolumesResource
        extends AbstractBackendCollectionResource<GlusterVolume, GlusterVolumeEntity>
        implements GlusterVolumesResource {

    private ClusterResource parent;
    private String clusterId;

    public BackendGlusterVolumesResource() {
        super(GlusterVolume.class, GlusterVolumeEntity.class);
    }

    public BackendGlusterVolumesResource(ClusterResource parent) {
        this();
        setParent(parent);
    }

    public BackendGlusterVolumesResource(ClusterResource parent, String clusterId) {
        this();
        setParent(parent);
        this.clusterId = clusterId;
    }

    public ClusterResource getParent() {
        return parent;
    }

    public void setParent(ClusterResource parent) {
        this.parent = parent;
    }

    @Override
    public GlusterVolumes list() {
        String constraint = QueryHelper.getConstraint(httpHeaders, uriInfo, "cluster = "
                + parent.get().getName(), GlusterVolume.class);
        return mapCollection(getBackendCollection(SearchType.GlusterVolume, constraint));
    }

    private GlusterVolumes mapCollection(List<GlusterVolumeEntity> entities) {
        GlusterVolumes collection = new GlusterVolumes();
        for (GlusterVolumeEntity entity : entities) {
            collection.getGlusterVolumes().add(addLinks(populate(map(entity), entity)));
        }
        return collection;
    }

    @Override
    protected GlusterVolume addParents(GlusterVolume volume) {
        volume.setCluster(new Cluster());
        volume.getCluster().setId(clusterId);
        return volume;
    }

    @Override
    public Response add(GlusterVolume volume) {
        validateParameters(volume, "name", "volumeType", "bricks");

        GlusterVolumeEntity volumeEntity = getMapper(GlusterVolume.class, GlusterVolumeEntity.class).map(volume, null);
        volumeEntity.setClusterId(asGuid(parent.get().getId()));
        mapBricks(volume, volumeEntity);

        return performCreate(ActionType.CreateGlusterVolume,
                new CreateGlusterVolumeParameters(volumeEntity, isForce()),
                new QueryIdResolver<Guid>(QueryType.GetGlusterVolumeById, IdQueryParameters.class),
                true);
    }

    private void mapBricks(GlusterVolume volume, GlusterVolumeEntity volumeEntity) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        for(GlusterBrick brick : volume.getBricks().getGlusterBricks()) {
            bricks.add(getMapper(GlusterBrick.class, GlusterBrickEntity.class).map(brick, null));
        }
        volumeEntity.setBricks(bricks);
    }

    @Override
    public GlusterVolumeResource getVolumeResource(String id) {
        return inject(new BackendGlusterVolumeResource(id, this));
    }
}
