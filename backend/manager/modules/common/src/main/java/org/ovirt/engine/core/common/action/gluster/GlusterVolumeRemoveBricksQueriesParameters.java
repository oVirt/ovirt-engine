package org.ovirt.engine.core.common.action.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.queries.gluster.GlusterVolumeQueriesParameters;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeRemoveBricksQueriesParameters extends GlusterVolumeQueriesParameters {
    private static final long serialVersionUID = -6327827215443668556L;
    private List<GlusterBrickEntity> bricks;

    public GlusterVolumeRemoveBricksQueriesParameters() {
    }

    public GlusterVolumeRemoveBricksQueriesParameters(Guid clusterId, Guid volumeId) {
        super(clusterId, volumeId);
    }

    public GlusterVolumeRemoveBricksQueriesParameters(Guid clusterId, Guid volumeId, List<GlusterBrickEntity> bricks) {
        super(clusterId, volumeId);
        this.bricks = bricks;
    }

    public List<GlusterBrickEntity> getBricks() {
        return bricks;
    }

    public void setBricks(List<GlusterBrickEntity> bricks) {
        this.bricks = bricks;
    }
}
