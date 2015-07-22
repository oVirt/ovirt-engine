package org.ovirt.engine.core.common.action.gluster;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.compat.Guid;

public class GlusterVolumeBricksParameters extends GlusterVolumeParameters {

    private static final long serialVersionUID = -3931242146910640331L;

    @Valid
    @NotNull(message = "VALIDATION_GLUSTER_VOLUME_BRICKS_NOT_NULL")
    private List<GlusterBrickEntity> bricks;

    public GlusterVolumeBricksParameters() {
    }

    public GlusterVolumeBricksParameters(Guid volumeId, List<GlusterBrickEntity> bricks) {
        super(volumeId);
        setBricks(bricks);
        setVolumeIdInBricks(bricks);
    }

    public void setBricks(List<GlusterBrickEntity> bricks) {
        this.bricks = bricks;
    }

    public List<GlusterBrickEntity> getBricks() {
        return bricks;
    }

    public void setVolumeIdInBricks(List<GlusterBrickEntity> bricks) {
        if (bricks != null) {
            for (GlusterBrickEntity brick : bricks) {
                brick.setVolumeId(getVolumeId());
            }
        }
    }
}
