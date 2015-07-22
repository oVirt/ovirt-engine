package org.ovirt.engine.core.common.action.gluster;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;

/**
 * Command parameters class with a volume id and brick list as parameters.
 */
public class GlusterVolumeRemoveBricksParameters extends GlusterVolumeParameters {

    private static final long serialVersionUID = -6327827215443668543L;

    @Valid
    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_BRICKS_NOT_NULL")
    private List<GlusterBrickEntity> bricks;

    private int replicaCount;

    public GlusterVolumeRemoveBricksParameters() {
    }

    public GlusterVolumeRemoveBricksParameters(Guid volumeId, List<GlusterBrickEntity> bricks) {
        super(volumeId);
        setBricks(bricks);
    }

    public GlusterVolumeRemoveBricksParameters(Guid volumeId, List<GlusterBrickEntity> bricks, int replicaCount) {
        super(volumeId);
        setBricks(bricks);
        setReplicaCount(replicaCount);
    }

    public List<GlusterBrickEntity> getBricks() {
        return bricks;
    }

    public void setBricks(List<GlusterBrickEntity> bricks) {
        this.bricks = bricks;
    }

    public int getReplicaCount() {
        return replicaCount;
    }

    public void setReplicaCount(int replicaCount) {
        this.replicaCount = replicaCount;
    }

}
